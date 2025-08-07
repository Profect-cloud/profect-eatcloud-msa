package com.eatcloud.paymentservice.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.eatcloud.paymentservice.entity.PaymentRequest;

import profect.eatcloud.global.timeData.BaseTimeRepository;

@Repository
public interface PaymentRequestRepository extends BaseTimeRepository<PaymentRequest, UUID> {
}