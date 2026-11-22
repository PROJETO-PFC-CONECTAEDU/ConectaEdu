package com.conectaedu.api.modules.user.genericUser.repository;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);

    Optional<User> findByEmailAndPasswordResetToken(String email, String token);

    Optional<User> findByEmailAndEmailConfirmationToken(String email, String token);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findBySearch(@Param("search") String search);
}
