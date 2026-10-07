package com.sabari.cartnova.entity;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /** Customers may cancel only before the order starts processing. */
    public boolean isCancellable() {
        return this == PLACED || this == CONFIRMED;
    }

    /** The allowed order lifecycle. DELIVERED and CANCELLED are final. */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PLACED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == SHIPPED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
