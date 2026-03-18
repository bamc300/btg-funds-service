package com.btg.funds.application.port.output;

import com.btg.funds.domain.model.NotificationChannel;

public interface NotificationSenderPort {
	void send(NotificationChannel channel, String destination, String message);
}
