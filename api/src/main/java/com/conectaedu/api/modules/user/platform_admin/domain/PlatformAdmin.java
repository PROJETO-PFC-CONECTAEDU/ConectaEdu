package com.conectaedu.api.modules.user.platform_admin.domain;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("PLATFORM_ADMIN")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PlatformAdmin extends User {
}
