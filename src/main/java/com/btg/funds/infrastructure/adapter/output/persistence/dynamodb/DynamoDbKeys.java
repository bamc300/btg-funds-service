package com.btg.funds.infrastructure.adapter.output.persistence.dynamodb;

public final class DynamoDbKeys {
	private DynamoDbKeys() {
	}

	public static String clientPk(String clientId) {
		return "CLIENT#" + clientId;
	}

	public static String profileSk() {
		return "PROFILE";
	}

	public static String subscriptionSk(String subscriptionId) {
		return "SUBSCRIPTION#" + subscriptionId;
	}

	public static String transactionSk(String isoInstant, String transactionId) {
		return "TX#" + isoInstant + "#" + transactionId;
	}
}
