package com.eatcloud.paymentservice.entity;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.globalCategory.entity.PaymentMethodCode;
import profect.eatcloud.domain.globalCategory.entity.PaymentStatusCode;
import profect.eatcloud.global.timeData.BaseTimeEntity;

@Entity
@Table(name = "p_payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "payment_id")
	private UUID paymentId;

	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount;

	@Column(name = "pg_transaction_id", length = 100)
	private String pgTransactionId;

	@Column(name = "approval_code", length = 50)
	private String approvalCode;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "card_info", columnDefinition = "jsonb")
	private Map<String, Object> cardInfo;

	@Column(name = "redirect_url", columnDefinition = "TEXT")
	private String redirectUrl;

	@Column(name = "receipt_url", columnDefinition = "TEXT")
	private String receiptUrl;

	@Column(name = "requested_at")
	private Timestamp requestedAt;

	@Column(name = "approved_at")
	private Timestamp approvedAt;

	@Column(name = "failed_at")
	private Timestamp failedAt;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@Column(name = "offline_payment_note", columnDefinition = "TEXT")
	private String offlinePaymentNote;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_request_id", nullable = false)
	private PaymentRequest paymentRequest;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_status", referencedColumnName = "code")
	private PaymentStatusCode paymentStatusCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_method", referencedColumnName = "code")
	private PaymentMethodCode paymentMethodCode;
}