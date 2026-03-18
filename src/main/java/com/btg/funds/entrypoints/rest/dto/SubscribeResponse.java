package com.btg.funds.entrypoints.rest.dto;

public record SubscribeResponse(
		String subscriptionId,
		String transactionId,
		long newBalanceCop
) {
}
