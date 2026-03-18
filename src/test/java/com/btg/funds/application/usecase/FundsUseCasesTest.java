package com.btg.funds.application.usecase;

import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.application.usecase.command.CancelSubscriptionCommand;
import com.btg.funds.application.usecase.command.SubscribeCommand;
import com.btg.funds.domain.exception.InsufficientBalanceException;
import com.btg.funds.domain.model.Client;
import com.btg.funds.domain.model.NotificationChannel;
import com.btg.funds.domain.model.TransactionType;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryClientRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryFundCatalogAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemorySubscriptionRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryTransactionRepositoryAdapter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FundsUseCasesTest {
	private static final class SequenceClock extends Clock {
		private final AtomicLong counter = new AtomicLong(0);
		private final Instant start;

		private SequenceClock(Instant start) {
			this.start = start;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return start.plusMillis(counter.getAndIncrement());
		}
	}

	@Test
	void subscribe_and_cancel_updates_balance_and_creates_transactions() {
		var clientRepo = new InMemoryClientRepositoryAdapter();
		var fundCatalog = new InMemoryFundCatalogAdapter();
		var subscriptionRepo = new InMemorySubscriptionRepositoryAdapter();
		var transactionRepo = new InMemoryTransactionRepositoryAdapter();
		Clock clock = new SequenceClock(Instant.parse("2026-01-01T00:00:00Z"));

		var lastNotification = new AtomicReference<String>();
		NotificationSenderPort notificationSender = (channel, destination, message) -> lastNotification.set(channel + "|" + destination + "|" + message);

		clientRepo.save(new Client("client-1", 500_000, NotificationChannel.EMAIL, "client1@example.com", "+573000000000"));

		var subscribe = new SubscribeToFundService(clientRepo, fundCatalog, subscriptionRepo, transactionRepo, notificationSender, clock);
		var cancel = new CancelSubscriptionService(clientRepo, subscriptionRepo, transactionRepo, clock);
		var listTransactions = new ListTransactionsService(clientRepo, transactionRepo);

		var subscriptionResult = subscribe.subscribe(new SubscribeCommand("client-1", 4, null, null, null));
		assertThat(subscriptionResult.newBalanceCop()).isEqualTo(250_000);
		assertThat(subscriptionResult.subscriptionId()).isNotBlank();
		assertThat(subscriptionResult.transactionId()).isNotBlank();
		assertThat(lastNotification.get()).contains("Suscripción exitosa al fondo FDO-ACCIONES");

		var cancelResult = cancel.cancel(new CancelSubscriptionCommand("client-1", subscriptionResult.subscriptionId()));
		assertThat(cancelResult.newBalanceCop()).isEqualTo(500_000);
		assertThat(cancelResult.transactionId()).isNotBlank();

		var transactions = listTransactions.listByClientId("client-1");
		assertThat(transactions).hasSize(2);
		assertThat(transactions.getFirst().type()).isEqualTo(TransactionType.CANCELLATION);
		assertThat(transactions.getLast().type()).isEqualTo(TransactionType.SUBSCRIPTION);
	}

	@Test
	void subscribe_without_balance_throws_required_message() {
		var clientRepo = new InMemoryClientRepositoryAdapter();
		var fundCatalog = new InMemoryFundCatalogAdapter();
		var subscriptionRepo = new InMemorySubscriptionRepositoryAdapter();
		var transactionRepo = new InMemoryTransactionRepositoryAdapter();
		Clock clock = new SequenceClock(Instant.parse("2026-01-01T00:00:00Z"));
		NotificationSenderPort notificationSender = (channel, destination, message) -> {
		};

		clientRepo.save(new Client("client-1", 100_000, NotificationChannel.EMAIL, "client1@example.com", "+573000000000"));

		var subscribe = new SubscribeToFundService(clientRepo, fundCatalog, subscriptionRepo, transactionRepo, notificationSender, clock);

		assertThatThrownBy(() -> subscribe.subscribe(new SubscribeCommand("client-1", 2, null, null, null)))
				.isInstanceOf(InsufficientBalanceException.class)
				.hasMessage("No tiene saldo disponible para vincularse al fondo FPV_BTG_PACTUAL_ECOPETROL");
	}
}
