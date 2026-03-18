package com.btg.funds.application.usecase;

import com.btg.funds.application.port.input.CancelSubscriptionUseCase;
import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.application.port.output.SubscriptionRepositoryPort;
import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.application.usecase.command.CancelSubscriptionCommand;
import com.btg.funds.application.usecase.result.CancellationResult;
import com.btg.funds.domain.exception.ClientNotFoundException;
import com.btg.funds.domain.exception.SubscriptionNotFoundException;
import com.btg.funds.domain.model.SubscriptionStatus;
import com.btg.funds.domain.model.Transaction;
import com.btg.funds.domain.model.TransactionType;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class CancelSubscriptionService implements CancelSubscriptionUseCase {
	private final ClientRepositoryPort clientRepository;
	private final SubscriptionRepositoryPort subscriptionRepository;
	private final TransactionRepositoryPort transactionRepository;
	private final Clock clock;

	public CancelSubscriptionService(
			ClientRepositoryPort clientRepository,
			SubscriptionRepositoryPort subscriptionRepository,
			TransactionRepositoryPort transactionRepository,
			Clock clock
	) {
		this.clientRepository = Objects.requireNonNull(clientRepository, "clientRepository");
		this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "subscriptionRepository");
		this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public CancellationResult cancel(CancelSubscriptionCommand command) {
		Objects.requireNonNull(command, "command");

		var client = clientRepository.findById(command.clientId()).orElseThrow(() -> new ClientNotFoundException(command.clientId()));
		var subscription = subscriptionRepository.findById(command.clientId(), command.subscriptionId())
				.orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId()));

		if (subscription.status() == SubscriptionStatus.CANCELLED) {
			throw new IllegalStateException("Subscription already cancelled: " + subscription.id());
		}

		Instant now = Instant.now(clock);
		var cancelled = subscription.cancel(now);
		subscriptionRepository.save(cancelled);

		var updatedClient = client.credit(subscription.amountCop());
		clientRepository.save(updatedClient);

		String transactionId = UUID.randomUUID().toString();
		var transaction = new Transaction(
				transactionId,
				client.id(),
				subscription.fundId(),
				TransactionType.CANCELLATION,
				subscription.amountCop(),
				subscription.id(),
				now
		);
		transactionRepository.save(transaction);

		return new CancellationResult(subscription.id(), transactionId, updatedClient.balanceCop());
	}
}
