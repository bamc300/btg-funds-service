package com.btg.funds.infrastructure.adapter.output.external;

import com.btg.funds.application.port.output.NotificationSenderPort;
import com.btg.funds.domain.model.NotificationChannel;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

public class SmtpNotificationSenderAdapter implements NotificationSenderPort {
	private final JavaMailSender mailSender;
	private final String fromEmail;

	public SmtpNotificationSenderAdapter(JavaMailSender mailSender, String fromEmail) {
		this.mailSender = Objects.requireNonNull(mailSender, "mailSender");
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

		var mail = new SimpleMailMessage();
		mail.setFrom(fromEmail);
		mail.setTo(destination);
		mail.setSubject("BTG Funds");
		mail.setText(message);
		mailSender.send(mail);
	}
}
