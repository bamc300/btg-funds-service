package com.btg.funds.domain.exception;

public class SubscriptionNotFoundException extends RuntimeException {
	public SubscriptionNotFoundException(String subscriptionId) {
		super("Subscription not found: " + subscriptionId);
	}
}
