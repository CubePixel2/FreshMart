package com.grocery.pos.model;

public enum PurchasePaymentStatus {
    UNPAID("Unpaid", "bg-danger"),
    PARTIAL("Partial", "bg-warning text-dark"),
    PAID("Paid in Full", "bg-success");

    private final String displayName;
    private final String badgeClass;

    PurchasePaymentStatus(String displayName, String badgeClass) {
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
