package com.fooddelivery.authservice.dto;

public class UpdateDriverProfileDTO {
    private String vehicleType;
    private String vehicleNumber;
    private String driverStatus;  // "AVAILABLE", "BUSY", "OFFLINE"

    // Getters and Setters
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getDriverStatus() { return driverStatus; }
    public void setDriverStatus(String driverStatus) { this.driverStatus = driverStatus; }
}