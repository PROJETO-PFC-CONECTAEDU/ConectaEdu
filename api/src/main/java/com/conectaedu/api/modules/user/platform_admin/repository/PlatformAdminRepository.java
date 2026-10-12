package com.conectaedu.api.modules.user.platform_admin.repository;

import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, UUID> {
}
