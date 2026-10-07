package com.sabari.cartnova.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Safely builds a Pageable from ?page=&size=&sort=field,dir query parameters. */
public final class PageRequestFactory {

    private static final Set<String> SORTABLE = Set.of("id", "name", "price", "stock", "createdAt");
    private static final int MAX_SIZE = 50;

    private PageRequestFactory() {
    }

    public static Pageable create(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        String field = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (SORTABLE.contains(parts[0].trim())) {
                field = parts[0].trim();
                direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
            }
        }
        // "id" as a tie-breaker keeps pagination stable when many rows share the sort value
        Sort order = Sort.by(direction, field).and(Sort.by(Sort.Direction.ASC, "id"));
        return PageRequest.of(safePage, safeSize, order);
    }
}
