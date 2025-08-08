package com.eatcloud.userservice.security.userDetails;

import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.eatcloud.userservice.admin.entity.Admin;
import com.eatcloud.userservice.admin.repository.AdminRepository;
import com.eatcloud.userservice.customer.entity.Customer;
import com.eatcloud.userservice.customer.repository.CustomerRepository;
import com.eatcloud.userservice.manager.entity.Manager;
import com.eatcloud.userservice.manager.repository.ManagerRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AdminRepository adminRepository;
	private final ManagerRepository managerRepository;
	private final CustomerRepository customerRepository;

	/**
	 * UUID와 사용자 타입("admin", "manager", "user")을 받아 해당 유저 정보를 반환합니다.
	 */
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(UUID id, String type) throws Throwable {
		switch (type.toLowerCase()) {
			case "admin":
				Admin admin = adminRepository.findById(id)
					.orElseThrow(() -> new UsernameNotFoundException("해당 Admin을 찾을 수 없습니다: " + id));
				return User.builder()
					.username(admin.getId().toString())
					.password(admin.getPassword())
					.roles("ADMIN")
					.build();

			case "manager":
				Manager manager = managerRepository.findById(id)
					.orElseThrow(() -> new UsernameNotFoundException("해당 Manager를 찾을 수 없습니다: " + id));
				return User.builder()
					.username(manager.getId().toString())
					.password(manager.getPassword())
					.roles("MANAGER")
					.build();
			case "customer":
				Customer customer = customerRepository.findById(id)
					.orElseThrow(() -> new UsernameNotFoundException("해당 Customer를 찾을 수 없습니다: " + id));
				return User.builder()
					.username(customer.getId().toString())
					.password(customer.getPassword())
					.roles("CUSTOMER")
					.build();

			default:
				throw new IllegalArgumentException("알 수 없는 사용자 타입입니다: " + type);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		throw new UnsupportedOperationException("loadUserByUsername(UUID id, String type)을 사용하세요.");
	}
}
