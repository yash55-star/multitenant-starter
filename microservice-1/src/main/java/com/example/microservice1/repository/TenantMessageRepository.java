package com.example.microservice1.repository;

import com.example.microservice1.entity.TenantMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMessageRepository extends JpaRepository<TenantMessage, Long> {
}
