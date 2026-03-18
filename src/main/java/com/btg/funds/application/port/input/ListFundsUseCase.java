package com.btg.funds.application.port.input;

import com.btg.funds.domain.model.Fund;

import java.util.List;

public interface ListFundsUseCase {
	List<Fund> listAll();
}
