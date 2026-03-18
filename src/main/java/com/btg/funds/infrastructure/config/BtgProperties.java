package com.btg.funds.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "btg")
public record BtgProperties(
		long initialBalanceCop,
		SecurityProperties security,
		PersistenceProperties persistence,
		NotificationsProperties notifications
) {
	public BtgProperties {
		if (security == null) {
			security = new SecurityProperties("btg", "btg");
		}
		if (persistence == null) {
			persistence = new PersistenceProperties(PersistenceType.INMEMORY, new DynamoDbProperties("btg-funds"));
		}
		if (notifications == null) {
			notifications = new NotificationsProperties(NotificationsMode.INMEMORY, new SesProperties(null), new SmtpProperties(null, null, null, null, true, true));
		}
	}

	public record SecurityProperties(
			String username,
			String password
	) {
	}

	public enum PersistenceType {
		INMEMORY,
		DYNAMODB
	}

	public record PersistenceProperties(
			PersistenceType type,
			DynamoDbProperties dynamodb
	) {
		public PersistenceProperties {
			if (type == null) {
				type = PersistenceType.INMEMORY;
			}
			if (dynamodb == null) {
				dynamodb = new DynamoDbProperties("btg-funds");
			}
		}
	}

	public record DynamoDbProperties(
			String tableName
	) {
	}

	public enum NotificationsMode {
		INMEMORY,
		SES,
		SMTP
	}

	public record NotificationsProperties(
			NotificationsMode mode,
			SesProperties ses,
			SmtpProperties smtp
	) {
		public NotificationsProperties {
			if (mode == null) {
				mode = NotificationsMode.INMEMORY;
			}
			if (ses == null) {
				ses = new SesProperties(null);
			}
			if (smtp == null) {
				smtp = new SmtpProperties(null, null, null, null, true, true);
			}
		}
	}

	public record SesProperties(
			String fromEmail
	) {
	}

	public record SmtpProperties(
			String host,
			Integer port,
			String username,
			String password,
			boolean auth,
			boolean starttls
	) {
	}
}
