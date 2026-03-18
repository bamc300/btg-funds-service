package com.btg.funds.entrypoints.rest.dto;

public record CancelResponse(
		String subscriptionId,
		String transactionId,
		long newBalanceCop
) {
}
