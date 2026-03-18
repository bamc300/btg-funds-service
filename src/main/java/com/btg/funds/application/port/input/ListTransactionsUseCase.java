package com.btg.funds.application.port.input;

import com.btg.funds.application.usecase.result.TransactionResult;

import java.util.List;

public interface ListTransactionsUseCase {
	List<TransactionResult> listByClientId(String clientId);
}
