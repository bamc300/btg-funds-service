package com.btg.funds.infrastructure.adapter.output.persistence.dynamodb;

import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.domain.model.Client;
import com.btg.funds.domain.model.NotificationChannel;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DynamoDbClientRepositoryAdapter implements ClientRepositoryPort {
	private final DynamoDbClient dynamodb;
	private final String tableName;

	public DynamoDbClientRepositoryAdapter(DynamoDbClient dynamodb, String tableName) {
		this.dynamodb = Objects.requireNonNull(dynamodb, "dynamodb");
		this.tableName = Objects.requireNonNull(tableName, "tableName");
	}

	@Override
	public Optional<Client> findById(String clientId) {
		var response = dynamodb.getItem(GetItemRequest.builder()
				.tableName(tableName)
				.key(Map.of(
						"PK", AttributeValue.fromS(DynamoDbKeys.clientPk(clientId)),
						"SK", AttributeValue.fromS(DynamoDbKeys.profileSk())
				))
				.consistentRead(true)
				.build());

		if (!response.hasItem()) {
			return Optional.empty();
		}

		return Optional.of(toClient(clientId, response.item()));
	}

	@Override
	public Client save(Client client) {
		dynamodb.putItem(PutItemRequest.builder()
				.tableName(tableName)
				.item(Map.of(
						"PK", AttributeValue.fromS(DynamoDbKeys.clientPk(client.id())),
						"SK", AttributeValue.fromS(DynamoDbKeys.profileSk()),
						"type", AttributeValue.fromS("CLIENT"),
						"clientId", AttributeValue.fromS(client.id()),
						"balanceCop", AttributeValue.fromN(Long.toString(client.balanceCop())),
						"notificationPreference", AttributeValue.fromS(client.notificationPreference().name()),
						"email", client.email() == null ? AttributeValue.fromNul(true) : AttributeValue.fromS(client.email()),
						"phone", client.phone() == null ? AttributeValue.fromNul(true) : AttributeValue.fromS(client.phone())
				))
				.build());
		return client;
	}

	private Client toClient(String clientId, Map<String, AttributeValue> item) {
		long balance = Long.parseLong(item.getOrDefault("balanceCop", AttributeValue.fromN("0")).n());
		var pref = NotificationChannel.valueOf(item.getOrDefault("notificationPreference", AttributeValue.fromS(NotificationChannel.EMAIL.name())).s());
		String email = item.containsKey("email") && item.get("email").nul() != null && item.get("email").nul() ? null : item.getOrDefault("email", AttributeValue.fromS("")).s();
		String phone = item.containsKey("phone") && item.get("phone").nul() != null && item.get("phone").nul() ? null : item.getOrDefault("phone", AttributeValue.fromS("")).s();

		return new Client(clientId, balance, pref, email == null || email.isBlank() ? null : email, phone == null || phone.isBlank() ? null : phone);
	}
}
