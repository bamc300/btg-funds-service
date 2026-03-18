package com.btg.funds.domain.model;

import java.util.Objects;

public record Client(
		String id,
		long balanceCop,
		NotificationChannel notificationPreference,
		String email,
		String phone
) {
	public Client {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(notificationPreference, "notificationPreference");
	}

	public Client debit(long amountCop) {
		if (amountCop < 0) {
			throw new IllegalArgumentException("amountCop must be >= 0");
		}
		return new Client(id, balanceCop - amountCop, notificationPreference, email, phone);
	}

	public Client credit(long amountCop) {
		if (amountCop < 0) {
			throw new IllegalArgumentException("amountCop must be >= 0");
		}
		return new Client(id, balanceCop + amountCop, notificationPreference, email, phone);
	}
}
