package com.eatcloud.paymentservice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import profect.eatcloud.global.timeData.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name = "p_payment_requests")
@NoArgsConstructor
public class PaymentRequest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_request_id")
    private java.util.UUID paymentRequestId;

    @Column(name = "order_id", nullable = false)
    private java.util.UUID orderId;

    @Column(name = "pg_provider", nullable = false, length = 100)
    private String pgProvider;

    @Column(name = "request_payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String requestPayload;

    @Column(name = "redirect_url", columnDefinition = "text")
    private String redirectUrl;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    public PaymentRequest(java.util.UUID orderId, String pgProvider, String requestPayload) {
        this.orderId = orderId;
        this.pgProvider = pgProvider;
        this.requestPayload = requestPayload;
        this.status = "PENDING";
        this.requestedAt = LocalDateTime.now();
    }

}