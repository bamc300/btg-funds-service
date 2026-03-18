package com.btg.funds.application.port.output;

import com.btg.funds.domain.model.Transaction;

import java.util.List;

public interface TransactionRepositoryPort {
	Transaction save(Transaction transaction);
	List<Transaction> findByClientId(String clientId);
}
