package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RealtimeEvent {

    private String id;
    private String type; // SALE_CREATED, SALE_RETURN_CREATED, PRODUCT_RETURN_CREATED, PURCHASE_POSTED, HEARTBEAT
    private String entityNumber;
    private BigDecimal amount;
    private String partyName; // Customer or Supplier name
    private String processedBy;
    private String timestamp;
    private String message;

    public RealtimeEvent() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public RealtimeEvent(String type, String entityNumber, BigDecimal amount, String partyName, String processedBy, String message) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.entityNumber = entityNumber;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.partyName = partyName != null ? partyName : "N/A";
        this.processedBy = processedBy != null ? processedBy : "System";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.message = message;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntityNumber() {
        return entityNumber;
    }

    public void setEntityNumber(String entityNumber) {
        this.entityNumber = entityNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
