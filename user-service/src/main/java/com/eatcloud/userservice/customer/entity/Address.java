package com.eatcloud.userservice.customer.entity;

import java.util.UUID;

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
import com.eatcloud.userservice.global.timeData.BaseTimeEntity;

@Entity
@Table(name = "p_addresses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "zipcode", length = 10)
	private String zipcode;

	@Column(name = "road_addr", length = 500)
	private String roadAddr;

	@Column(name = "detail_addr", length = 200)
	private String detailAddr;

	@Column(name = "is_selected")
	@Builder.Default
	private Boolean isSelected = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	public void updateAddress(String zipcode, String roadAddr, String detailAddr) {
		this.zipcode = zipcode;
		this.roadAddr = roadAddr;
		this.detailAddr = detailAddr;
	}

	public void changeSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}