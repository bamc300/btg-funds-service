package com.btg.funds.infrastructure.adapter.output.persistence.inmemory;

import com.btg.funds.application.port.output.FundCatalogPort;
import com.btg.funds.domain.model.Fund;
import com.btg.funds.domain.model.FundCategory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryFundCatalogAdapter implements FundCatalogPort {
	private final Map<Integer, Fund> funds = Map.of(
			1, new Fund(1, "FPV_BTG_PACTUAL_RECAUDADORA", 75_000, FundCategory.FPV),
			2, new Fund(2, "FPV_BTG_PACTUAL_ECOPETROL", 125_000, FundCategory.FPV),
			3, new Fund(3, "DEUDAPRIVADA", 50_000, FundCategory.FIC),
			4, new Fund(4, "FDO-ACCIONES", 250_000, FundCategory.FIC),
			5, new Fund(5, "FPV_BTG_PACTUAL_DINAMICA", 100_000, FundCategory.FPV)
	);

	@Override
	public Optional<Fund> findById(int fundId) {
		return Optional.ofNullable(funds.get(fundId));
	}

	@Override
	public List<Fund> listAll() {
		return funds.values().stream().toList();
	}
}
