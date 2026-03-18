package com.btg.funds.domain.model;

public record Fund(
		int id,
		String name,
		long minimumAmountCop,
		FundCategory category
) {
}
