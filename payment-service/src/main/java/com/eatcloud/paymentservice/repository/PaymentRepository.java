package com.eatcloud.paymentservice.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.eatcloud.paymentservice.entity.Payment;

import profect.eatcloud.global.timeData.BaseTimeRepository;

@Repository
public interface PaymentRepository extends BaseTimeRepository<Payment, UUID> {
    
}