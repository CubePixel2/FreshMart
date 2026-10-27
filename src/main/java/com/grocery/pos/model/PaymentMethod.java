package com.grocery.pos.model;

public enum PaymentMethod {
    CASH("Cash on Counter", "bi-cash-coin", "Cashier receives physical currency and returns change"),
    UPI("UPI (GPay / PhonePe / Paytm)", "bi-qr-code-scan", "Standard UPI payment"),
    CARD("Debit / Credit Card", "bi-credit-card", "Standard card payment"),
    ONLINE("Online Gateway (Mock / Real)", "bi-globe", "Integrated online payment gateway checkout"),
    UPI_QR("Dynamic UPI QR (GPay / PhonePe / Paytm)", "bi-qr-code-scan", "Live dynamic QR scanned by customer via any UPI app"),
    RAZORPAY("Razorpay Gateway (Cards / UPI / NetBanking)", "bi-shield-check", "All-in-one payment gateway for cards, UPI, wallets and netbanking"),
    STRIPE("Stripe Card Gateway", "bi-credit-card-2-front", "Secure credit/debit card processing via Stripe"),
    CARD_POS("POS Card Terminal (EDC)", "bi-credit-card", "Card swipe / tap on counter POS EDC terminal");

    private final String displayName;
    private final String iconClass;
    private final String description;

    PaymentMethod(String displayName, String iconClass, String description) {
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getDescription() {
        return description;
    }
}
