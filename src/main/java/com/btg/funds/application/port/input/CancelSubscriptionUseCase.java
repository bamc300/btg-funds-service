package com.btg.funds.application.port.input;

import com.btg.funds.application.usecase.command.CancelSubscriptionCommand;
import com.btg.funds.application.usecase.result.CancellationResult;

public interface CancelSubscriptionUseCase {
	CancellationResult cancel(CancelSubscriptionCommand command);
}
