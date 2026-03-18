package com.btg.funds.application.port.input;

import com.btg.funds.application.usecase.command.CreateClientCommand;
import com.btg.funds.domain.model.Client;

public interface CreateClientUseCase {
	Client create(CreateClientCommand command);
}
