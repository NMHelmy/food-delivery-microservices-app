package com.fooddelivery.deliveryservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverDTO {

    @NotNull(message = "Driver ID is required")
    private Long driverId;
}
