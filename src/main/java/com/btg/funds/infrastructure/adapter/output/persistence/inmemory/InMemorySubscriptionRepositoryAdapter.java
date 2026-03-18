package com.btg.funds.infrastructure.adapter.output.persistence.inmemory;

import com.btg.funds.application.port.output.SubscriptionRepositoryPort;
import com.btg.funds.domain.model.Subscription;
import com.btg.funds.domain.model.SubscriptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {
	private final ConcurrentHashMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

	@Override
	public Optional<Subscription> findById(String clientId, String subscriptionId) {
		var subscription = subscriptions.get(subscriptionId);
		if (subscription == null) {
			return Optional.empty();
		}
		if (!subscription.clientId().equals(clientId)) {
			return Optional.empty();
		}
		return Optional.of(subscription);
	}

	@Override
	public Subscription save(Subscription subscription) {
		subscriptions.put(subscription.id(), subscription);
		return subscription;
	}

	@Override
	public List<Subscription> findActiveByClientId(String clientId) {
		return subscriptions.values().stream()
				.filter(s -> s.clientId().equals(clientId))
				.filter(s -> s.status() == SubscriptionStatus.ACTIVE)
				.toList();
	}
}
