package com.btg.funds.infrastructure.adapter.output.external;

import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.domain.model.NotificationChannel;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.Objects;

public class SesNotificationSenderAdapter implements NotificationSenderPort {
	private final SesClient ses;
	private final String fromEmail;

	public SesNotificationSenderAdapter(SesClient ses, String fromEmail) {
		this.ses = Objects.requireNonNull(ses, "ses");
		this.fromEmail = Objects.requireNonNull(fromEmail, "fromEmail");
	}

	@Override
	public void send(NotificationChannel channel, String destination, String message) {
		Objects.requireNonNull(channel, "channel");
		Objects.requireNonNull(destination, "destination");
		Objects.requireNonNull(message, "message");

		if (channel != NotificationChannel.EMAIL) {
			return;
		}

		var request = SendEmailRequest.builder()
				.source(fromEmail)
				.destination(Destination.builder().toAddresses(destination).build())
				.message(Message.builder()
						.subject(Content.builder().data("BTG Funds").build())
						.body(Body.builder().text(Content.builder().data(message).build()).build())
						.build())
				.build();

		ses.sendEmail(request);
	}
}
