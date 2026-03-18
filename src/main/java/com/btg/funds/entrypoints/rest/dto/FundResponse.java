package com.btg.funds.entrypoints.rest.dto;

import com.btg.funds.domain.model.FundCategory;

public record FundResponse(
		int id,
		String name,
		long minimumAmountCop,
		FundCategory category
) {
}
