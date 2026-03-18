package com.btg.funds.entrypoints.rest;

import com.btg.funds.application.port.input.ListTransactionsUseCase;
import com.btg.funds.entrypoints.rest.dto.TransactionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clients/{clientId}/transactions")
public class TransactionController {
	private final ListTransactionsUseCase listTransactionsUseCase;

	public TransactionController(ListTransactionsUseCase listTransactionsUseCase) {
		this.listTransactionsUseCase = listTransactionsUseCase;
	}

	@GetMapping
	public List<TransactionResponse> list(@PathVariable String clientId) {
		return listTransactionsUseCase.listByClientId(clientId).stream()
				.map(tx -> new TransactionResponse(tx.id(), tx.fundId(), tx.type(), tx.amountCop(), tx.subscriptionId(), tx.occurredAt()))
				.toList();
	}
}
