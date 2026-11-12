package com.conectaedu.api.modules.user.genericUser.domain;

import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_role", discriminatorType = DiscriminatorType.STRING)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "user_role", insertable=false, updatable=false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    private String passwordResetToken;
    
    private LocalDateTime passwordResetExpiresAt;
    
    private String emailConfirmationToken;
    
    private boolean emailConfirmed = false;
    
    public void generatePasswordResetToken() {
        this.passwordResetToken = java.util.UUID.randomUUID().toString();
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(1);
    }

    public boolean isPasswordResetTokenExpired() {
        return this.passwordResetExpiresAt != null && this.passwordResetExpiresAt.isBefore(LocalDateTime.now());
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void generateEmailConfirmationToken() {
        this.emailConfirmationToken = java.util.UUID.randomUUID().toString();
    }

    public void confirmEmail() {
        this.emailConfirmed = true;
        this.emailConfirmationToken = null;
        this.updatedAt = LocalDateTime.now();
    }
}
