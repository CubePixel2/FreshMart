package com.grocery.pos.model;

public enum PaymentMethod {
    CASH("Cash (INR)", "bi-cash-coin"),
    UPI("UPI (GPay / PhonePe / Paytm)", "bi-qr-code-scan"),
    CARD("Debit / Credit Card", "bi-credit-card");

    private final String displayName;
    private final String iconClass;

    PaymentMethod(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }
}
