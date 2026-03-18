package com.btg.funds.entrypoints.rest;

import com.btg.funds.application.port.input.CreateClientUseCase;
import com.btg.funds.application.usecase.command.CreateClientCommand;
import com.btg.funds.entrypoints.rest.dto.ClientResponse;
import com.btg.funds.entrypoints.rest.dto.CreateClientRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {
	private final CreateClientUseCase createClientUseCase;

	public ClientController(CreateClientUseCase createClientUseCase) {
		this.createClientUseCase = createClientUseCase;
	}

	@PostMapping
	public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
		var client = createClientUseCase.create(new CreateClientCommand(
				request.clientId(),
				request.notificationPreference(),
				request.email(),
				request.phone()
		));

		return ResponseEntity.status(HttpStatus.CREATED).body(new ClientResponse(
				client.id(),
				client.balanceCop(),
				client.notificationPreference(),
				client.email(),
				client.phone()
		));
	}
}
