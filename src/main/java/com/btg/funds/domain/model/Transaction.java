package com.btg.funds.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Transaction(
		String id,
		String clientId,
		int fundId,
		TransactionType type,
		long amountCop,
		String subscriptionId,
		Instant occurredAt
) {
	public Transaction {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(clientId, "clientId");
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(subscriptionId, "subscriptionId");
		Objects.requireNonNull(occurredAt, "occurredAt");
	}
}
