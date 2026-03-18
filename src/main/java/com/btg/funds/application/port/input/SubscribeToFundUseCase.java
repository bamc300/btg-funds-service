package com.btg.funds.application.port.input;

import com.btg.funds.application.usecase.command.SubscribeCommand;
import com.btg.funds.application.usecase.result.SubscriptionResult;

public interface SubscribeToFundUseCase {
	SubscriptionResult subscribe(SubscribeCommand command);
}
