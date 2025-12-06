package com.fooddelivery.userservice.dto;

import jakarta.validation.constraints.NotNull;

public class OwnerProfileDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String businessName;
    private String businessAddress;
    private String taxId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
}