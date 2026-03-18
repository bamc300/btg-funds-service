package com.btg.funds.entrypoints.rest.dto;

import com.btg.funds.domain.model.NotificationChannel;

public record ClientResponse(
		String id,
		long balanceCop,
		NotificationChannel notificationPreference,
		String email,
		String phone
) {
}
