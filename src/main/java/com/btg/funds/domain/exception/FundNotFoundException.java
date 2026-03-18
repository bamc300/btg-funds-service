package com.btg.funds.domain.exception;

public class FundNotFoundException extends RuntimeException {
	public FundNotFoundException(int fundId) {
		super("Fund not found: " + fundId);
	}
}
