package com.grocery.pos.model;

public enum PurchaseOrderStatus {
    DRAFT("Draft", "bg-secondary"),
    ORDERED("Ordered", "bg-primary"),
    PARTIALLY_RECEIVED("Partially Received", "bg-warning text-dark"),
    RECEIVED("Received", "bg-success"),
    CANCELLED("Cancelled", "bg-danger");

    private final String displayName;
    private final String badgeClass;

    PurchaseOrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
