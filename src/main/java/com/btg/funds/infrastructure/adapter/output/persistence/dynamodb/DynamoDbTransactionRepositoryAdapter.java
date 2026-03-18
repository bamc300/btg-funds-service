package com.btg.funds.infrastructure.adapter.output.persistence.dynamodb;

import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.domain.model.Transaction;
import com.btg.funds.domain.model.TransactionType;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DynamoDbTransactionRepositoryAdapter implements TransactionRepositoryPort {
	private final DynamoDbClient dynamodb;
	private final String tableName;

	public DynamoDbTransactionRepositoryAdapter(DynamoDbClient dynamodb, String tableName) {
		this.dynamodb = Objects.requireNonNull(dynamodb, "dynamodb");
		this.tableName = Objects.requireNonNull(tableName, "tableName");
	}

	@Override
	public Transaction save(Transaction transaction) {
		dynamodb.putItem(PutItemRequest.builder()
				.tableName(tableName)
				.item(Map.of(
						"PK", AttributeValue.fromS(DynamoDbKeys.clientPk(transaction.clientId())),
						"SK", AttributeValue.fromS(DynamoDbKeys.transactionSk(transaction.occurredAt().toString(), transaction.id())),
						"type", AttributeValue.fromS("TX"),
						"txId", AttributeValue.fromS(transaction.id()),
						"clientId", AttributeValue.fromS(transaction.clientId()),
						"fundId", AttributeValue.fromN(Integer.toString(transaction.fundId())),
						"txType", AttributeValue.fromS(transaction.type().name()),
						"amountCop", AttributeValue.fromN(Long.toString(transaction.amountCop())),
						"subscriptionId", AttributeValue.fromS(transaction.subscriptionId()),
						"occurredAt", AttributeValue.fromS(transaction.occurredAt().toString())
				))
				.build());
		return transaction;
	}

	@Override
	public List<Transaction> findByClientId(String clientId) {
		var response = dynamodb.query(QueryRequest.builder()
				.tableName(tableName)
				.keyConditionExpression("PK = :pk AND begins_with(SK, :sk)")
				.expressionAttributeValues(Map.of(
						":pk", AttributeValue.fromS(DynamoDbKeys.clientPk(clientId)),
						":sk", AttributeValue.fromS("TX#")
				))
				.scanIndexForward(false)
				.build());

		return response.items().stream()
				.map(this::toTransaction)
				.toList();
	}

	private Transaction toTransaction(Map<String, AttributeValue> item) {
		String id = item.get("txId").s();
		String clientId = item.get("clientId").s();
		int fundId = Integer.parseInt(item.get("fundId").n());
		var type = TransactionType.valueOf(item.get("txType").s());
		long amount = Long.parseLong(item.get("amountCop").n());
		String subscriptionId = item.get("subscriptionId").s();
		Instant occurredAt = Instant.parse(item.get("occurredAt").s());
		return new Transaction(id, clientId, fundId, type, amount, subscriptionId, occurredAt);
	}
}

