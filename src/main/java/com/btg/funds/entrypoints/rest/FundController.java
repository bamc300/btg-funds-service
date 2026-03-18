package com.btg.funds.entrypoints.rest;

import com.btg.funds.application.port.input.ListFundsUseCase;
import com.btg.funds.entrypoints.rest.dto.FundResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/funds")
public class FundController {
	private final ListFundsUseCase listFundsUseCase;

	public FundController(ListFundsUseCase listFundsUseCase) {
		this.listFundsUseCase = listFundsUseCase;
	}

	@GetMapping
	public List<FundResponse> list() {
		return listFundsUseCase.listAll().stream()
				.map(f -> new FundResponse(f.id(), f.name(), f.minimumAmountCop(), f.category()))
				.toList();
	}
}
