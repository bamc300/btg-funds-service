package com.btg.funds.entrypoints.rest.dto;

import com.btg.funds.domain.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClientRequest(
		@NotBlank String clientId,
		@NotNull NotificationChannel notificationPreference,
		String email,
		String phone
) {
}
