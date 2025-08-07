package com.eatcloud.orderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/health")
    public String health() {
        return "Order Service Connected! ✅";
    }

    @GetMapping
    public String getOrders() {
        return "Order Service is working! 🚀";
    }
}