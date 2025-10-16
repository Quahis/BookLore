package com.adityachandel.booklore.repository;

import com.adityachandel.booklore.model.entity.EmailProviderV2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailProviderV2Repository extends JpaRepository<EmailProviderV2Entity, Long> {

    Optional<EmailProviderV2Entity> findByIdAndUserId(Long id, Long userId);

    List<EmailProviderV2Entity> findAllByUserId(Long userId);

    @Modifying
    @Query("UPDATE EmailProviderV2Entity e SET e.defaultProvider = false")
    void updateAllProvidersToNonDefault();

    @Query("SELECT e FROM EmailProviderV2Entity e WHERE e.userId = :userId AND e.defaultProvider = true")
    Optional<EmailProviderV2Entity> findDefaultEmailProvider(@Param("userId") Long userId);

    @Query("SELECT e FROM EmailProviderV2Entity e WHERE e.shared = true AND e.userId IN (SELECT u.id FROM BookLoreUserEntity u WHERE u.permissions.permissionAdmin = true)")
    List<EmailProviderV2Entity> findAllBySharedTrueAndAdmin();

    @Query("SELECT e FROM EmailProviderV2Entity e WHERE e.id = :id AND e.shared = true AND e.userId IN (SELECT u.id FROM BookLoreUserEntity u WHERE u.permissions.permissionAdmin = true)")
    Optional<EmailProviderV2Entity> findSharedProviderById(@Param("id") Long id);
}
