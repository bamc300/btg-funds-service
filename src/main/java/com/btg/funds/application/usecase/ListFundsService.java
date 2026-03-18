package com.btg.funds.application.usecase;

import com.btg.funds.application.port.input.ListFundsUseCase;
import com.btg.funds.application.port.output.FundCatalogPort;
import com.btg.funds.domain.model.Fund;

import java.util.List;
import java.util.Objects;

public class ListFundsService implements ListFundsUseCase {
	private final FundCatalogPort fundCatalog;

	public ListFundsService(FundCatalogPort fundCatalog) {
		this.fundCatalog = Objects.requireNonNull(fundCatalog, "fundCatalog");
	}

	@Override
	public List<Fund> listAll() {
		return fundCatalog.listAll();
	}
}
