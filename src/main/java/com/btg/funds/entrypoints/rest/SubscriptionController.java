package com.btg.funds.entrypoints.rest;

import com.btg.funds.application.port.input.CancelSubscriptionUseCase;
import com.btg.funds.application.port.input.SubscribeToFundUseCase;
import com.btg.funds.application.usecase.command.CancelSubscriptionCommand;
import com.btg.funds.application.usecase.command.SubscribeCommand;
import com.btg.funds.entrypoints.rest.dto.CancelResponse;
import com.btg.funds.entrypoints.rest.dto.SubscribeRequest;
import com.btg.funds.entrypoints.rest.dto.SubscribeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients/{clientId}")
public class SubscriptionController {
	private final SubscribeToFundUseCase subscribeToFundUseCase;
	private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

	public SubscriptionController(SubscribeToFundUseCase subscribeToFundUseCase, CancelSubscriptionUseCase cancelSubscriptionUseCase) {
		this.subscribeToFundUseCase = subscribeToFundUseCase;
		this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
	}

	@PostMapping("/funds/{fundId}/subscriptions")
	public ResponseEntity<SubscribeResponse> subscribe(
			@PathVariable String clientId,
			@PathVariable int fundId,
			@Valid @RequestBody(required = false) SubscribeRequest request
	) {
		var cmd = new SubscribeCommand(
				clientId,
				fundId,
				request == null ? null : request.amountCop(),
				request == null ? null : request.notificationChannelOverride(),
				request == null ? null : request.destinationOverride()
		);

		var result = subscribeToFundUseCase.subscribe(cmd);
		return ResponseEntity.status(HttpStatus.CREATED).body(new SubscribeResponse(
				result.subscriptionId(),
				result.transactionId(),
				result.newBalanceCop()
		));
	}

	@DeleteMapping("/subscriptions/{subscriptionId}")
	public CancelResponse cancel(@PathVariable String clientId, @PathVariable String subscriptionId) {
		var result = cancelSubscriptionUseCase.cancel(new CancelSubscriptionCommand(clientId, subscriptionId));
		return new CancelResponse(result.subscriptionId(), result.transactionId(), result.newBalanceCop());
	}
}
