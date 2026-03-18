package com.btg.funds.infrastructure.adapter.output.persistence.inmemory;

import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.domain.model.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryTransactionRepositoryAdapter implements TransactionRepositoryPort {
	private final ConcurrentHashMap<String, CopyOnWriteArrayList<Transaction>> byClientId = new ConcurrentHashMap<>();

	@Override
	public Transaction save(Transaction transaction) {
		byClientId.computeIfAbsent(transaction.clientId(), ignored -> new CopyOnWriteArrayList<>()).add(transaction);
		return transaction;
	}

	@Override
	public List<Transaction> findByClientId(String clientId) {
		return byClientId.getOrDefault(clientId, new CopyOnWriteArrayList<>()).stream()
				.sorted(Comparator.comparing(Transaction::occurredAt).reversed())
				.toList();
	}
}
