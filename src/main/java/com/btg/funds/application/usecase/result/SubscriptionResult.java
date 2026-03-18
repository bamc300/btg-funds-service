package com.btg.funds.application.usecase.result;

public record SubscriptionResult(
		String subscriptionId,
		String transactionId,
		long newBalanceCop
) {
}
