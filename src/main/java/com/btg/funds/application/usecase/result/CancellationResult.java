package com.btg.funds.application.usecase.result;

public record CancellationResult(
		String subscriptionId,
		String transactionId,
		long newBalanceCop
) {
}
