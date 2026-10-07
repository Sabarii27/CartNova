package com.sabari.cartnova.config;

import com.sabari.cartnova.entity.Category;
import com.sabari.cartnova.entity.Product;
import com.sabari.cartnova.entity.User;
import com.sabari.cartnova.entity.UserRole;
import com.sabari.cartnova.repository.CategoryRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inserts demo data on first start (only into empty tables). LOCAL DEVELOPMENT ONLY.
 * Passwords are hashed with BCrypt before they are stored. Disable with SEED_ENABLED=false.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-name}")
    private String adminName;
    @Value("${app.seed.admin-email}")
    private String adminEmail;
    @Value("${app.seed.admin-password}")
    private String adminPassword;

    /** Where the browser can reach this backend. Product images are served from /images/products/. */
    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    private static final Map<String, String> IMAGE_FILES = Map.ofEntries(
            Map.entry("Nova X14 Laptop", "laptop.svg"),
            Map.entry("Pulse Wireless Earbuds", "earbuds.svg"),
            Map.entry("Orbit Smartphone 128GB", "smartphone.svg"),
            Map.entry("Beam 27 inch Monitor", "monitor.svg"),
            Map.entry("Classic Denim Jacket", "denim-jacket.svg"),
            Map.entry("Everyday Running Shoes", "running-shoes.svg"),
            Map.entry("Urban Canvas Backpack", "backpack.svg"),
            Map.entry("Brew Master Coffee Maker", "coffee-maker.svg"),
            Map.entry("Steel Cookware Set (5 pcs)", "cookware-set.svg"),
            Map.entry("Clean Code in Practice", "book-clean-code.svg"),
            Map.entry("Spring Boot in Depth", "book-spring-boot.svg"),
            Map.entry("Pro Yoga Mat 6mm", "yoga-mat.svg"),
            Map.entry("Adjustable Dumbbell Pair", "dumbbells.svg"));

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedCatalogue();
        fixSeedProductImages();
    }

    /**
     * Gives every demo product the matching local illustration. Also repairs databases that were seeded
     * earlier with random placeholder photos. Images that an admin set to something else are left alone.
     */
    private void fixSeedProductImages() {
        for (Product p : productRepository.findAll()) {
            String file = IMAGE_FILES.get(p.getName());
            if (file == null) {
                continue;
            }
            String current = p.getImageUrl();
            if (current == null || current.isBlank() || current.contains("picsum.photos")) {
                p.setImageUrl(publicBaseUrl + "/images/products/" + file);
                productRepository.save(p);
            }
        }
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        createUser(adminName, adminEmail, adminPassword, UserRole.ADMIN);
        createUser("Sabari Customer", "user@cartnova.com", "User@123", UserRole.USER);
        createUser("Priya Sharma", "priya@cartnova.com", "Priya@123", UserRole.USER);
        log.info("Seeded demo users. Admin login: {} (local development only)", adminEmail);
    }

    private void createUser(String name, String email, String rawPassword, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }

    private void seedCatalogue() {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }
        Map<String, Category> cats = new LinkedHashMap<>();
        cats.put("Electronics", category("Electronics", "Phones, laptops and accessories"));
        cats.put("Fashion", category("Fashion", "Clothing, shoes and bags"));
        cats.put("Home & Kitchen", category("Home & Kitchen", "Appliances and everyday essentials"));
        cats.put("Books", category("Books", "Fiction, tech and self-improvement"));
        cats.put("Sports", category("Sports", "Fitness and outdoor gear"));

        product(cats.get("Electronics"), "Nova X14 Laptop", "14-inch laptop with 16 GB RAM, 512 GB SSD and an all-day battery. Great for study and work.", "54999.00", 15);
        product(cats.get("Electronics"), "Pulse Wireless Earbuds", "Bluetooth 5.3 earbuds with active noise cancellation and a 24-hour charging case.", "2499.00", 40);
        product(cats.get("Electronics"), "Orbit Smartphone 128GB", "6.5-inch AMOLED display, 50 MP camera and fast charging in a slim body.", "18999.00", 25);
        product(cats.get("Electronics"), "Beam 27 inch Monitor", "27-inch Full HD IPS monitor with thin bezels and a 75 Hz refresh rate.", "10499.00", 3);
        product(cats.get("Fashion"), "Classic Denim Jacket", "Mid-weight cotton denim jacket with a regular fit. Easy to layer in any season.", "2199.00", 30);
        product(cats.get("Fashion"), "Everyday Running Shoes", "Lightweight cushioned running shoes with a breathable mesh upper.", "3299.00", 50);
        product(cats.get("Fashion"), "Urban Canvas Backpack", "22-litre backpack with a padded laptop sleeve and water-resistant fabric.", "1599.00", 35);
        product(cats.get("Home & Kitchen"), "Brew Master Coffee Maker", "Programmable 10-cup drip coffee maker with a reusable filter and keep-warm plate.", "3799.00", 20);
        product(cats.get("Home & Kitchen"), "Steel Cookware Set (5 pcs)", "Stainless steel pots and pans with glass lids, suitable for all hob types.", "4599.00", 12);
        product(cats.get("Books"), "Clean Code in Practice", "A practical guide to writing readable, maintainable software with real-world examples.", "699.00", 60);
        product(cats.get("Books"), "Spring Boot in Depth", "Build production-ready REST APIs with Spring Boot, JPA and Spring Security.", "899.00", 45);
        product(cats.get("Sports"), "Pro Yoga Mat 6mm", "Non-slip 6 mm yoga mat with a carry strap. Dense cushioning for joints.", "999.00", 0);
        product(cats.get("Sports"), "Adjustable Dumbbell Pair", "Pair of adjustable dumbbells, 2 kg to 12 kg each, with a secure locking dial.", "5499.00", 8);

        log.info("Seeded {} categories and {} products", categoryRepository.count(), productRepository.count());
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private void product(Category category, String name, String description, String price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setImageUrl(publicBaseUrl + "/images/products/" + IMAGE_FILES.get(name));
        p.setCategory(category);
        productRepository.save(p);
    }
}
