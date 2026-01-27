package com.fooddelivery.deliveryservice.dto;

import com.fooddelivery.deliveryservice.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusDTO {

    @NotNull(message = "Status is required")
    private DeliveryStatus status;
}
