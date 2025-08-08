package com.eatcloud.userservice.admin.repository;

import java.util.Optional;
import java.util.UUID;

import com.eatcloud.userservice.admin.entity.Admin;
import com.eatcloud.userservice.global.timeData.BaseTimeRepository;

public interface AdminRepository extends BaseTimeRepository<Admin, UUID> {
	Optional<Admin> findByEmail(String email);
}