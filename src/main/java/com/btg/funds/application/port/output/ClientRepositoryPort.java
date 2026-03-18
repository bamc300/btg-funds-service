package com.btg.funds.application.port.output;

import com.btg.funds.domain.model.Client;

import java.util.Optional;

public interface ClientRepositoryPort {
	Optional<Client> findById(String clientId);
	Client save(Client client);
}
