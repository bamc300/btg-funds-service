package com.btg.funds.application.usecase;

import com.btg.funds.application.port.input.CreateClientUseCase;
import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.application.usecase.command.CreateClientCommand;
import com.btg.funds.domain.model.Client;

import java.util.Objects;

public class CreateClientService implements CreateClientUseCase {
	private final ClientRepositoryPort clientRepository;
	private final long initialBalanceCop;

	public CreateClientService(ClientRepositoryPort clientRepository, long initialBalanceCop) {
		this.clientRepository = Objects.requireNonNull(clientRepository, "clientRepository");
		this.initialBalanceCop = initialBalanceCop;
	}

	@Override
	public Client create(CreateClientCommand command) {
		Objects.requireNonNull(command, "command");

		var client = new Client(
				command.clientId(),
				initialBalanceCop,
				command.notificationPreference(),
				command.email(),
				command.phone()
		);

		return clientRepository.save(client);
	}
}
