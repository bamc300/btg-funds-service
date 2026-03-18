package com.btg.funds.entrypoints.rest.dto;

import com.btg.funds.domain.model.TransactionType;

import java.time.Instant;

public record TransactionResponse(
		String id,
		int fundId,
		TransactionType type,
		long amountCop,
		String subscriptionId,
		Instant occurredAt
) {
}
