package com.btg.funds.application.usecase.command;

public record CancelSubscriptionCommand(
		String clientId,
		String subscriptionId
) {
}
