package com.btg.funds.entrypoints.rest.dto;

import java.time.Instant;

public record ErrorResponse(
		Instant timestamp,
		String error,
		String message,
		String path
) {
}
