package com.btg.funds.infrastructure.config;

import com.btg.funds.application.port.input.CancelSubscriptionUseCase;
import com.btg.funds.application.port.input.CreateClientUseCase;
import com.btg.funds.application.port.input.ListFundsUseCase;
import com.btg.funds.application.port.input.ListTransactionsUseCase;
import com.btg.funds.application.port.input.SubscribeToFundUseCase;
import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.application.port.output.FundCatalogPort;
import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.application.port.output.SubscriptionRepositoryPort;
import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.application.usecase.CancelSubscriptionService;
import com.btg.funds.application.usecase.CreateClientService;
import com.btg.funds.application.usecase.ListFundsService;
import com.btg.funds.application.usecase.ListTransactionsService;
import com.btg.funds.application.usecase.SubscribeToFundService;
import com.btg.funds.infrastructure.adapter.output.external.InMemoryNotificationSenderAdapter;
import com.btg.funds.infrastructure.adapter.output.external.SesNotificationSenderAdapter;
import com.btg.funds.infrastructure.adapter.output.external.SmtpNotificationSenderAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.dynamodb.DynamoDbClientRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.dynamodb.DynamoDbSubscriptionRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.dynamodb.DynamoDbTransactionRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryClientRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryFundCatalogAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemorySubscriptionRepositoryAdapter;
import com.btg.funds.infrastructure.adapter.output.persistence.inmemory.InMemoryTransactionRepositoryAdapter;
import com.btg.funds.domain.model.NotificationChannel;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(BtgProperties.class)
public class ApplicationConfig {
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public ClientRepositoryPort clientRepositoryPort(BtgProperties properties) {
		if (properties.persistence().type() == BtgProperties.PersistenceType.DYNAMODB) {
			return new DynamoDbClientRepositoryAdapter(dynamoDbClient(), properties.persistence().dynamodb().tableName());
		}
		return new InMemoryClientRepositoryAdapter();
	}

	@Bean
	public FundCatalogPort fundCatalogPort() {
		return new InMemoryFundCatalogAdapter();
	}

	@Bean
	public SubscriptionRepositoryPort subscriptionRepositoryPort(BtgProperties properties) {
		if (properties.persistence().type() == BtgProperties.PersistenceType.DYNAMODB) {
			return new DynamoDbSubscriptionRepositoryAdapter(dynamoDbClient(), properties.persistence().dynamodb().tableName());
		}
		return new InMemorySubscriptionRepositoryAdapter();
	}

	@Bean
	public TransactionRepositoryPort transactionRepositoryPort(BtgProperties properties) {
		if (properties.persistence().type() == BtgProperties.PersistenceType.DYNAMODB) {
			return new DynamoDbTransactionRepositoryAdapter(dynamoDbClient(), properties.persistence().dynamodb().tableName());
		}
		return new InMemoryTransactionRepositoryAdapter();
	}

	@Bean
	public NotificationSenderPort notificationSenderPort(BtgProperties properties) {
		if (properties.notifications().mode() == BtgProperties.NotificationsMode.SES) {
			var fromEmail = properties.notifications().ses().fromEmail();
			if (fromEmail == null || fromEmail.isBlank()) {
				throw new IllegalStateException("btg.notifications.ses.from-email is required when btg.notifications.mode=SES");
			}
			return new SesNotificationSenderAdapter(sesClient(), fromEmail);
		}
		if (properties.notifications().mode() == BtgProperties.NotificationsMode.SMTP) {
			var smtp = properties.notifications().smtp();
			if (smtp.host() == null || smtp.host().isBlank()) {
				throw new IllegalStateException("btg.notifications.smtp.host is required when btg.notifications.mode=SMTP");
			}
			if (smtp.port() == null) {
				throw new IllegalStateException("btg.notifications.smtp.port is required when btg.notifications.mode=SMTP");
			}
			if (smtp.username() == null || smtp.username().isBlank()) {
				throw new IllegalStateException("btg.notifications.smtp.username is required when btg.notifications.mode=SMTP");
			}
			if (smtp.password() == null || smtp.password().isBlank()) {
				throw new IllegalStateException("btg.notifications.smtp.password is required when btg.notifications.mode=SMTP");
			}

			var sender = new JavaMailSenderImpl();
			sender.setHost(smtp.host());
			sender.setPort(smtp.port());
			sender.setUsername(smtp.username());
			sender.setPassword(smtp.password());
			var props = sender.getJavaMailProperties();
			props.put("mail.smtp.auth", Boolean.toString(smtp.auth()));
			props.put("mail.smtp.starttls.enable", Boolean.toString(smtp.starttls()));
			return new SmtpNotificationSenderAdapter(sender, smtp.username());
		}
		return new InMemoryNotificationSenderAdapter();
	}

	private DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
				.region(awsRegion())
				.build();
	}

	private SesClient sesClient() {
		return SesClient.builder()
				.region(awsRegion())
				.build();
	}

	private Region awsRegion() {
		String region = System.getenv("AWS_REGION");
		if (region == null || region.isBlank()) {
			region = System.getenv("AWS_DEFAULT_REGION");
		}
		if (region == null || region.isBlank()) {
			throw new IllegalStateException("AWS region is required (AWS_REGION or AWS_DEFAULT_REGION)");
		}
		return Region.of(region);
	}

	@Bean
	public CreateClientUseCase createClientUseCase(ClientRepositoryPort clientRepositoryPort, BtgProperties properties) {
		return new CreateClientService(clientRepositoryPort, properties.initialBalanceCop());
	}

	@Bean
	public ApplicationRunner seedDefaultClient(ClientRepositoryPort clientRepositoryPort, BtgProperties properties) {
		return args -> clientRepositoryPort.findById("client-1").orElseGet(() -> clientRepositoryPort.save(
				new com.btg.funds.domain.model.Client(
						"client-1",
						properties.initialBalanceCop(),
						NotificationChannel.EMAIL,
						"client1@example.com",
						"+573000000000"
				)
		));
	}

	@Bean
	public SubscribeToFundUseCase subscribeToFundUseCase(
			ClientRepositoryPort clientRepositoryPort,
			FundCatalogPort fundCatalogPort,
			SubscriptionRepositoryPort subscriptionRepositoryPort,
			TransactionRepositoryPort transactionRepositoryPort,
			NotificationSenderPort notificationSenderPort,
			Clock clock
	) {
		return new SubscribeToFundService(
				clientRepositoryPort,
				fundCatalogPort,
				subscriptionRepositoryPort,
				transactionRepositoryPort,
				notificationSenderPort,
				clock
		);
	}

	@Bean
	public CancelSubscriptionUseCase cancelSubscriptionUseCase(
			ClientRepositoryPort clientRepositoryPort,
			SubscriptionRepositoryPort subscriptionRepositoryPort,
			TransactionRepositoryPort transactionRepositoryPort,
			Clock clock
	) {
		return new CancelSubscriptionService(clientRepositoryPort, subscriptionRepositoryPort, transactionRepositoryPort, clock);
	}

	@Bean
	public ListTransactionsUseCase listTransactionsUseCase(
			ClientRepositoryPort clientRepositoryPort,
			TransactionRepositoryPort transactionRepositoryPort
	) {
		return new ListTransactionsService(clientRepositoryPort, transactionRepositoryPort);
	}

	@Bean
	public ListFundsUseCase listFundsUseCase(FundCatalogPort fundCatalogPort) {
		return new ListFundsService(fundCatalogPort);
	}
}
