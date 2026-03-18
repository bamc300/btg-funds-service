package com.btg.funds.application.port.output;

import com.btg.funds.domain.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepositoryPort {
	Optional<Subscription> findById(String clientId, String subscriptionId);
	Subscription save(Subscription subscription);
	List<Subscription> findActiveByClientId(String clientId);
}
