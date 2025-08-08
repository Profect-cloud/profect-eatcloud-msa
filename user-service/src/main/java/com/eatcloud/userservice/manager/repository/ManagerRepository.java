package com.eatcloud.userservice.manager.repository;

import java.util.Optional;
import java.util.UUID;

import com.eatcloud.userservice.manager.entity.Manager;
import com.eatcloud.userservice.global.timeData.BaseTimeRepository;

public interface ManagerRepository extends BaseTimeRepository<Manager, UUID> {
	Optional<Manager> findByEmail(String email);
}
