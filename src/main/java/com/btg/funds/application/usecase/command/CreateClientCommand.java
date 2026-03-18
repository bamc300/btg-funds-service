package com.btg.funds.application.usecase.command;

import com.btg.funds.domain.model.NotificationChannel;

public record CreateClientCommand(
		String clientId,
		NotificationChannel notificationPreference,
		String email,
		String phone
) {
}
