package com.btg.funds.infrastructure.adapter.output.persistence.inmemory;

import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.domain.model.Client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientRepositoryAdapter implements ClientRepositoryPort {
	private final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();

	@Override
	public Optional<Client> findById(String clientId) {
		return Optional.ofNullable(clients.get(clientId));
	}

	@Override
	public Client save(Client client) {
		clients.put(client.id(), client);
		return client;
	}
}
