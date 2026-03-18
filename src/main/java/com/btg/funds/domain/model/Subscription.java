package com.btg.funds.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Subscription(
		String id,
		String clientId,
		int fundId,
		long amountCop,
		SubscriptionStatus status,
		Instant createdAt,
		Instant cancelledAt
) {
	public Subscription {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(clientId, "clientId");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(createdAt, "createdAt");
	}

	public Subscription cancel(Instant when) {
		Objects.requireNonNull(when, "when");
		return new Subscription(id, clientId, fundId, amountCop, SubscriptionStatus.CANCELLED, createdAt, when);
	}
}
