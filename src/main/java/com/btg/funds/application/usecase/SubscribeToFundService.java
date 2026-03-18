package com.btg.funds.application.usecase;

import com.btg.funds.application.port.input.SubscribeToFundUseCase;
import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.application.port.output.FundCatalogPort;
import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.application.port.output.SubscriptionRepositoryPort;
import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.application.usecase.command.SubscribeCommand;
import com.btg.funds.application.usecase.result.SubscriptionResult;
import com.btg.funds.domain.exception.ClientNotFoundException;
import com.btg.funds.domain.exception.FundNotFoundException;
import com.btg.funds.domain.exception.InsufficientBalanceException;
import com.btg.funds.domain.model.Subscription;
import com.btg.funds.domain.model.SubscriptionStatus;
import com.btg.funds.domain.model.Transaction;
import com.btg.funds.domain.model.TransactionType;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SubscribeToFundService implements SubscribeToFundUseCase {
	private final ClientRepositoryPort clientRepository;
	private final FundCatalogPort fundCatalog;
	private final SubscriptionRepositoryPort subscriptionRepository;
	private final TransactionRepositoryPort transactionRepository;
	private final NotificationSenderPort notificationSender;
	private final Clock clock;

	public SubscribeToFundService(
			ClientRepositoryPort clientRepository,
			FundCatalogPort fundCatalog,
			SubscriptionRepositoryPort subscriptionRepository,
			TransactionRepositoryPort transactionRepository,
			NotificationSenderPort notificationSender,
			Clock clock
	) {
		this.clientRepository = Objects.requireNonNull(clientRepository, "clientRepository");
		this.fundCatalog = Objects.requireNonNull(fundCatalog, "fundCatalog");
		this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "subscriptionRepository");
		this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository");
		this.notificationSender = Objects.requireNonNull(notificationSender, "notificationSender");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public SubscriptionResult subscribe(SubscribeCommand command) {
		Objects.requireNonNull(command, "command");

		var fund = fundCatalog.findById(command.fundId()).orElseThrow(() -> new FundNotFoundException(command.fundId()));
		var client = clientRepository.findById(command.clientId()).orElseThrow(() -> new ClientNotFoundException(command.clientId()));

		long amountCop = command.amountCop() == null ? fund.minimumAmountCop() : command.amountCop();
		if (amountCop < fund.minimumAmountCop()) {
			throw new IllegalArgumentException("amountCop must be >= fund.minimumAmountCop");
		}

		if (client.balanceCop() < amountCop) {
			throw new InsufficientBalanceException(fund.name());
		}

		Instant now = Instant.now(clock);
		String subscriptionId = UUID.randomUUID().toString();

		var subscription = new Subscription(
				subscriptionId,
				client.id(),
				fund.id(),
				amountCop,
				SubscriptionStatus.ACTIVE,
				now,
				null
		);

		var updatedClient = client.debit(amountCop);
		clientRepository.save(updatedClient);
		subscriptionRepository.save(subscription);

		String transactionId = UUID.randomUUID().toString();
		var transaction = new Transaction(
				transactionId,
				client.id(),
				fund.id(),
				TransactionType.SUBSCRIPTION,
				amountCop,
				subscriptionId,
				now
		);
		transactionRepository.save(transaction);

		var channel = command.notificationChannelOverride() == null ? client.notificationPreference() : command.notificationChannelOverride();
		var destination = command.destinationOverride();
		if (destination == null || destination.isBlank()) {
			destination = channel == com.btg.funds.domain.model.NotificationChannel.EMAIL ? client.email() : client.phone();
		}
		var message = "Suscripción exitosa al fondo " + fund.name() + " por COP $" + amountCop;
		notificationSender.send(channel, destination, message);

		return new SubscriptionResult(subscriptionId, transactionId, updatedClient.balanceCop());
	}
}
