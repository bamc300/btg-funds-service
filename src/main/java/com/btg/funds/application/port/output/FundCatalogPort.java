package com.btg.funds.application.port.output;

import com.btg.funds.domain.model.Fund;

import java.util.List;
import java.util.Optional;

public interface FundCatalogPort {
	Optional<Fund> findById(int fundId);
	List<Fund> listAll();
}
