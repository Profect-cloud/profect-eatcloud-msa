package com.eatcloud.userservice.admin.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.eatcloud.userservice.admin.entity.ManagerStoreApplication;
import com.eatcloud.userservice.global.timeData.BaseTimeRepository;

@Repository
public interface ManagerStoreApplicationRepository extends BaseTimeRepository<ManagerStoreApplication, UUID> {
	boolean existsByManagerEmail(String managerEmail);
}