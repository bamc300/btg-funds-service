package com.btg.funds.infrastructure.adapter.output.persistence.dynamodb;

import com.btg.funds.application.port.output.SubscriptionRepositoryPort;
import com.btg.funds.domain.model.Subscription;
import com.btg.funds.domain.model.SubscriptionStatus;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DynamoDbSubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {
	private final DynamoDbClient dynamodb;
	private final String tableName;

	public DynamoDbSubscriptionRepositoryAdapter(DynamoDbClient dynamodb, String tableName) {
		this.dynamodb = Objects.requireNonNull(dynamodb, "dynamodb");
		this.tableName = Objects.requireNonNull(tableName, "tableName");
	}

	@Override
	public Optional<Subscription> findById(String clientId, String subscriptionId) {
		var response = dynamodb.getItem(GetItemRequest.builder()
				.tableName(tableName)
				.key(Map.of(
						"PK", AttributeValue.fromS(DynamoDbKeys.clientPk(clientId)),
						"SK", AttributeValue.fromS(DynamoDbKeys.subscriptionSk(subscriptionId))
				))
				.consistentRead(true)
				.build());

		if (!response.hasItem()) {
			return Optional.empty();
		}
		return Optional.of(toSubscription(subscriptionId, response.item()));
	}

	@Override
	public Subscription save(Subscription subscription) {
		dynamodb.putItem(PutItemRequest.builder()
				.tableName(tableName)
				.item(Map.of(
						"PK", AttributeValue.fromS(DynamoDbKeys.clientPk(subscription.clientId())),
						"SK", AttributeValue.fromS(DynamoDbKeys.subscriptionSk(subscription.id())),
						"type", AttributeValue.fromS("SUBSCRIPTION"),
						"subscriptionId", AttributeValue.fromS(subscription.id()),
						"clientId", AttributeValue.fromS(subscription.clientId()),
						"fundId", AttributeValue.fromN(Integer.toString(subscription.fundId())),
						"amountCop", AttributeValue.fromN(Long.toString(subscription.amountCop())),
						"status", AttributeValue.fromS(subscription.status().name()),
						"createdAt", AttributeValue.fromS(subscription.createdAt().toString()),
						"cancelledAt", subscription.cancelledAt() == null ? AttributeValue.fromNul(true) : AttributeValue.fromS(subscription.cancelledAt().toString())
				))
				.build());
		return subscription;
	}

	@Override
	public List<Subscription> findActiveByClientId(String clientId) {
		var response = dynamodb.query(QueryRequest.builder()
				.tableName(tableName)
				.keyConditionExpression("PK = :pk AND begins_with(SK, :sk)")
				.filterExpression("#s = :active")
				.expressionAttributeNames(Map.of("#s", "status"))
				.expressionAttributeValues(Map.of(
						":pk", AttributeValue.fromS(DynamoDbKeys.clientPk(clientId)),
						":sk", AttributeValue.fromS("SUBSCRIPTION#"),
						":active", AttributeValue.fromS(SubscriptionStatus.ACTIVE.name())
				))
				.build());

		return response.items().stream()
				.map(item -> toSubscription(item.get("subscriptionId").s(), item))
				.toList();
	}

	private Subscription toSubscription(String subscriptionId, Map<String, AttributeValue> item) {
		String clientId = item.get("clientId").s();
		int fundId = Integer.parseInt(item.get("fundId").n());
		long amount = Long.parseLong(item.get("amountCop").n());
		var status = SubscriptionStatus.valueOf(item.get("status").s());
		var createdAt = Instant.parse(item.get("createdAt").s());
		Instant cancelledAt = null;
		if (item.containsKey("cancelledAt") && (item.get("cancelledAt").nul() == null || !item.get("cancelledAt").nul())) {
			cancelledAt = Instant.parse(item.get("cancelledAt").s());
		}
		return new Subscription(subscriptionId, clientId, fundId, amount, status, createdAt, cancelledAt);
	}
}

