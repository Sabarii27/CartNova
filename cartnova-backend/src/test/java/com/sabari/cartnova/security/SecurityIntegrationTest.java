package com.sabari.cartnova.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests against the real Spring context, security filters and an in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private JsonNode postJson(String url, String token, String json, int expectedStatus) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) req.header("Authorization", "Bearer " + token);
        String body = mockMvc.perform(req).andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : objectMapper.readTree(body);
    }

    private String login(String email, String password) throws Exception {
        return postJson("/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", 200).get("token").asText();
    }

    @Test
    void publicProductEndpointNeedsNoToken() throws Exception {
        mockMvc.perform(get("/api/products?size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").isNumber());
        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsRejectMissingAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.path", is("/api/cart")));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        postJson("/api/auth/login", null, "{\"email\":\"user@cartnova.com\",\"password\":\"wrong\"}", 401);
    }

    @Test
    void userCannotAccessAdminEndpoints() throws Exception {
        String token = login("user@cartnova.com", "User@123");
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpointsButNotTheCart() throws Exception {
        String token = login("admin@cartnova.com", "Admin@123");
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").isNumber());
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationAlwaysCreatesUserRoleEvenIfAdminIsRequested() throws Exception {
        String json = "{\"name\":\"Mallory\",\"email\":\"mallory@example.com\",\"password\":\"secret123\",\"role\":\"ADMIN\"}";
        JsonNode created = postJson("/api/auth/register", null, json, 201);
        assertEquals("USER", created.get("role").asText());

        // the same email again is a conflict
        postJson("/api/auth/register", null, json, 409);
    }

    @Test
    void registrationValidationReturnsClearErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void adminProductWriteRequiresAdminRole() throws Exception {
        String body = "{\"name\":\"Test Item\",\"description\":\"A test product item\",\"price\":99.50,\"stock\":3,\"categoryId\":1}";
        postJson("/api/admin/products", null, body, 401);
        postJson("/api/admin/products", login("user@cartnova.com", "User@123"), body, 403);
        JsonNode created = postJson("/api/admin/products", login("admin@cartnova.com", "Admin@123"), body, 201);
        assertEquals("Test Item", created.get("name").asText());
    }

    @Test
    void fullShoppingFlow_checkoutReducesStock_andCancelRestoresIt() throws Exception {
        String token = login("user@cartnova.com", "User@123");

        // pick an in-stock Books product
        JsonNode page = objectMapper.readTree(mockMvc.perform(get("/api/products?category=Books&available=true&size=1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
                .andReturn().getResponse().getContentAsString());
        long productId = page.get("content").get(0).get("id").asLong();
        int stockBefore = page.get("content").get(0).get("stock").asInt();

        postJson("/api/cart/items", token, "{\"productId\":" + productId + ",\"quantity\":2}", 200);
        JsonNode order = postJson("/api/orders", token, "{\"shippingAddress\":\"12 Main Street, Chennai 600001\"}", 201);
        long orderId = order.get("id").asLong();
        assertEquals("PLACED", order.get("status").asText());

        int stockAfterOrder = stockOf(productId);
        assertEquals(stockBefore - 2, stockAfterOrder);

        // cart is empty after checkout
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items", hasSize(0)));

        // cancel returns the stock
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status", is("CANCELLED")));
        assertEquals(stockBefore, stockOf(productId));

        // another user cannot see this order
        String other = login("priya@cartnova.com", "Priya@123");
        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
    }

    private int stockOf(long productId) throws Exception {
        String body = mockMvc.perform(get("/api/products/" + productId)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("stock").asInt();
    }
}
