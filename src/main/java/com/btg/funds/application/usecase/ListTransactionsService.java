package com.btg.funds.application.usecase;

import com.btg.funds.application.port.input.ListTransactionsUseCase;
import com.btg.funds.application.port.output.ClientRepositoryPort;
import com.btg.funds.application.port.output.TransactionRepositoryPort;
import com.btg.funds.application.usecase.result.TransactionResult;
import com.btg.funds.domain.exception.ClientNotFoundException;

import java.util.List;
import java.util.Objects;

public class ListTransactionsService implements ListTransactionsUseCase {
	private final ClientRepositoryPort clientRepository;
	private final TransactionRepositoryPort transactionRepository;

	public ListTransactionsService(
			ClientRepositoryPort clientRepository,
			TransactionRepositoryPort transactionRepository
	) {
		this.clientRepository = Objects.requireNonNull(clientRepository, "clientRepository");
		this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository");
	}

	@Override
	public List<TransactionResult> listByClientId(String clientId) {
		Objects.requireNonNull(clientId, "clientId");
		clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));

		return transactionRepository.findByClientId(clientId).stream()
				.map(tx -> new TransactionResult(
						tx.id(),
						tx.fundId(),
						tx.type(),
						tx.amountCop(),
						tx.subscriptionId(),
						tx.occurredAt()
				))
				.toList();
	}
}
