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

import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_role", discriminatorType = DiscriminatorType.STRING)
@SQLRestriction("deleted = false")
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

    //Nascem inativos para confirmação do administrador do ConectaEdu
    private boolean active = false;

    private boolean deleted = false;

    private LocalDateTime updatedAt;
    
    private String passwordResetToken;
    
    private LocalDateTime passwordResetExpiresAt;
    
    private String emailConfirmationToken;
    
    private boolean emailConfirmed = false;
    
    private String secret2fa;

    private boolean is2faEnabled = false;

    private String disable2faCode;

    private LocalDateTime lastEmailConfirmationSentAt;

    private LocalDateTime lastPasswordResetSentAt;

    private LocalDateTime last2faCodeSentAt;
    
    public void generatePasswordResetToken() {
        this.passwordResetToken = String.format("%06d", new java.util.Random().nextInt(1000000));
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(1);
        this.lastPasswordResetSentAt = LocalDateTime.now();
    }

    public boolean canRequestPasswordReset() {
        if (this.lastPasswordResetSentAt == null) {
            return true;
        }
        return this.lastPasswordResetSentAt.isBefore(LocalDateTime.now().minusMinutes(1));
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
        this.emailConfirmationToken = String.format("%06d", new java.util.Random().nextInt(1000000));
        this.lastEmailConfirmationSentAt = LocalDateTime.now();
    }

    public boolean canResendEmailConfirmation() {
        if (this.lastEmailConfirmationSentAt == null) {
            return true;
        }
        return this.lastEmailConfirmationSentAt.isBefore(LocalDateTime.now().minusMinutes(1));
    }

    public boolean canRequest2faDisable() {
        if (this.last2faCodeSentAt == null) {
            return true;
        }
        return this.last2faCodeSentAt.isBefore(LocalDateTime.now().minusMinutes(1));
    }

    public void generate2faDisableCode() {
        this.disable2faCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        this.last2faCodeSentAt = LocalDateTime.now();
    }

    public void confirmEmail() {
        this.emailConfirmed = true;
        this.emailConfirmationToken = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void anonymize() {
        this.name = "Usuário Anonimizado";
        this.email = "deleted_" + this.id + "@conectaedu.com.br";
        this.password = "********";
        this.active = false;
        this.deleted = true;
        this.emailConfirmed = false;
        this.is2faEnabled = false;
        this.secret2fa = null;
        this.updatedAt = LocalDateTime.now();
    }
}
