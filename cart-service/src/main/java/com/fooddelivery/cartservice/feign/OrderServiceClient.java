package com.fooddelivery.cartservice.feign;

import com.fooddelivery.cartservice.dto.CreateOrderFromCartDTO;
import com.fooddelivery.cartservice.dto.OrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderServiceClient {

    @PostMapping("/orders/from-cart")
    OrderResponseDTO createOrderFromCart(
            @RequestBody CreateOrderFromCartDTO dto,
            @RequestHeader("X-Internal-Request") String internalFlag);
}
