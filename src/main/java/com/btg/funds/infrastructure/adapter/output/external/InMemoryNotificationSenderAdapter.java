package com.btg.funds.infrastructure.adapter.output.external;

import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.domain.model.NotificationChannel;

public class InMemoryNotificationSenderAdapter implements NotificationSenderPort {
	@Override
	public void send(NotificationChannel channel, String destination, String message) {
	}
}
