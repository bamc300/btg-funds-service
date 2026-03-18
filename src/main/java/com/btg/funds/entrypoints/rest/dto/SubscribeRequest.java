package com.btg.funds.entrypoints.rest.dto;

import com.btg.funds.domain.model.NotificationChannel;

public record SubscribeRequest(
		Long amountCop,
		NotificationChannel notificationChannelOverride,
		String destinationOverride
) {
}
