package com.btg.funds.application.usecase.result;

import com.btg.funds.domain.model.TransactionType;

import java.time.Instant;

public record TransactionResult(
		String id,
		int fundId,
		TransactionType type,
		long amountCop,
		String subscriptionId,
		Instant occurredAt
) {
}
