package com.btg.funds.application.usecase.command;

import com.btg.funds.domain.model.NotificationChannel;

public record SubscribeCommand(
		String clientId,
		int fundId,
		Long amountCop,
		NotificationChannel notificationChannelOverride,
		String destinationOverride
) {
}
