# CartNova Frontend

React + Vite single-page app for the CartNova e-commerce platform.
It talks to the Spring Boot backend **only through REST APIs** (`http://localhost:8080/api`).

## Run in VS Code
```bash
npm install
npm run dev        # http://localhost:5173
```
Optional: copy `.env.example` to `.env` to change the API URL.

## Build
```bash
npm run build      # outputs dist/
```

## Structure
```
src/
  components/   Navbar, Footer, ProductCard, Pagination, ProtectedRoute, Loader, ErrorMessage, EmptyState, StatusBadge
  context/      AuthContext (JWT + user), CartContext (server-backed cart)
  hooks/        useFetch
  layouts/      MainLayout, AdminLayout
  pages/        storefront pages + pages/admin
  services/     api.js (Axios + interceptors) and one module per API area
  utils/        formatting helpers
```

## Security note
`ProtectedRoute` only hides pages in the UI. The real protection is Spring Security on the backend;
anyone can edit client-side code, but they cannot get past a 401/403 from the server.
The JWT is kept in `localStorage` for this learning project (simple, but readable by any XSS bug).

## API contract this frontend expects
| Method | Path | Body / Notes | Returns |
|---|---|---|---|
| POST | /auth/register | {name, email, password} | {token, userId, name, email, role} |
| POST | /auth/login | {email, password} | same as above |
| GET | /profile | | {id, name, email, role, createdAt} |
| GET | /categories | | [{id, name, description}] |
| GET | /products | search, category, minPrice, maxPrice, available, page, size, sort=field,dir | Spring Page: {content, totalPages, totalElements, number, size} |
| GET | /products/{id} | | {id, name, description, price, stock, imageUrl, categoryId, categoryName, createdAt} |
| GET | /cart | | {id, items:[{id, productId, productName, imageUrl, unitPrice, quantity, subtotal, stock}], totalItems, totalAmount} |
| POST | /cart/items | {productId, quantity} | CartResponse |
| PUT | /cart/items/{id} | {quantity} | CartResponse |
| DELETE | /cart/items/{id} | | CartResponse |
| DELETE | /cart | | 204 |
| POST | /orders | {shippingAddress} | OrderResponse |
| GET | /orders | | [OrderResponse] |
| GET | /orders/{id} | | OrderResponse |
| PUT | /orders/{id}/cancel | | OrderResponse |
| GET | /admin/dashboard | | {totalRevenue, totalOrders, pendingOrders, totalProducts, lowStockProducts, totalUsers} |
| POST/PUT | /admin/products[/{id}] | {name, description, price, stock, imageUrl, categoryId} | ProductResponse |
| DELETE | /admin/products/{id} | | 204 |
| GET | /admin/users, /admin/users/{id} | | [{id, name, email, role, createdAt}] |
| GET | /admin/orders, /admin/orders/{id} | | OrderResponse |
| PUT | /admin/orders/{id}/status | {status} | OrderResponse |

`OrderResponse` = `{id, userId, userName, userEmail, totalAmount, status, shippingAddress, createdAt, updatedAt, items:[{id, productId, productName, imageUrl, quantity, unitPrice, subtotal}]}`

Errors: `{timestamp, status, message, path}` (validation errors may also include `errors: {field: message}`).
