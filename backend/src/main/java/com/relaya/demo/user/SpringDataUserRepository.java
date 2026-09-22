package com.relaya.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

	Optional<UserJpaEntity> findByEmail(String email);

	Optional<UserJpaEntity> findByTenantIdAndEmail(UUID tenantId, String email);

	List<UserJpaEntity> findByTenantId(UUID tenantId);
}