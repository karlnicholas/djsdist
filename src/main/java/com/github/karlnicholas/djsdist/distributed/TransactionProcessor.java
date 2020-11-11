package com.github.karlnicholas.djsdist.distributed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import org.lognet.springboot.grpc.GRpcService;

import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.journal.BillingCyclePosting;
import com.github.karlnicholas.djsdist.journal.LoanFundingPosting;
import com.github.karlnicholas.djsdist.journal.PostingFunctions;
import com.github.karlnicholas.djsdist.model.BillingCycle;
import com.github.karlnicholas.djsdist.model.Loan;
import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.github.karlnicholas.djsdist.repository.BillingCycleRepository;
import com.github.karlnicholas.djsdist.repository.LoanRepository;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.repository.TransactionRejectedRepository;
import com.github.karlnicholas.djsdist.repository.TransactionSubmittedRepository;
import com.github.karlnicholas.djsdist.service.PostingReader;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

@GRpcService
public class TransactionProcessor extends TransactionProcessorGrpc.TransactionProcessorImplBase {
	private final TransactionSubmittedRepository transactionSubmittedRepository;
	private final TransactionOpenRepository transactionOpenRepository;
	private final BillingCycleRepository billingCycleRepository;
	private final TransactionRejectedRepository transactionRejectedRepository;
	private final LoanRepository loanRepository;
	private final PostingReader postingReader;

	public TransactionProcessor(TransactionSubmittedRepository transactionSubmittedRepository,
			TransactionOpenRepository transactionOpenRepository, 
			BillingCycleRepository billingCycleRepository, 
			PostingReader postingReader,
			TransactionRejectedRepository transactionRejectedRepository, 
			LoanRepository loanRepository
	) {
		this.transactionSubmittedRepository = transactionSubmittedRepository;
		this.transactionOpenRepository = transactionOpenRepository;
		this.billingCycleRepository = billingCycleRepository;
		this.loanRepository = loanRepository;
		this.postingReader = postingReader;
		this.transactionRejectedRepository = transactionRejectedRepository;
	}

	@Override
	public void validateAndProcess(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Map<String, ByteString> results = new HashMap<>();
		results.putAll(request.getResultsMap());
		LocalDate businessDate = LocalDate.parse(request.getParamsOrThrow("businessDate").toStringUtf8());
		transactionSubmittedRepository.findById(Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8()))
			.ifPresent(transactionSubmitted -> {
				if (transactionSubmitted.getTransactionType() != null) {
					PostingFunctions payload = postingReader.instancePayload(transactionSubmitted);
					if (payload.validate()) {
						results.put("validated", ByteString.copyFromUtf8(Boolean.TRUE.toString()));
						TransactionOpen transactionOpen = transactionOpenRepository
								.save(TransactionOpen.builder()
										.accountId(transactionSubmitted.getAccountId())
										.businessDate(businessDate)
										.transactionDate(payload.retrieveTransactionDate())
										.payload(transactionSubmitted.getPayload())
										.transactionType(transactionSubmitted.getTransactionType())
										.version(transactionSubmitted.getVersion()).build());
						results.put("transactionType", ByteString.copyFromUtf8(transactionOpen.getTransactionType().toString()));
						results.put("transactionId", ByteString.copyFromUtf8(transactionOpen.getId().toString()));
						results.put("accountId", ByteString.copyFromUtf8(transactionOpen.getAccountId().toString()));
					}
				} else {
					transactionRejectedRepository.save(transactionSubmitted);
				}
				transactionSubmittedRepository.delete(transactionSubmitted);
			});
		
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(results)
				.build());
        responseObserver.onCompleted();
	}

	@Override
	public void accountFunded(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		transactionOpenRepository.findById(Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8()))
		.ifPresent(transaction -> {
				LoanFundingPosting loanFundPosting = postingReader.readValue(transaction,
						LoanFundingPosting.class);
				Loan loan = Loan.builder().principal(loanFundPosting.getPrincipal())
						.interestRate(loanFundPosting.getInterestRate())
						.inceptionDate(loanFundPosting.getInceptionDate())
						.termMonths(loanFundPosting.getTermMonths()).build();
				// P * r * (1 + r) n / [(1 + r) n – 1]
				BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(12));
				BigDecimal powR = BigDecimal.ONE.add(monthlyRate).pow(loan.getTermMonths());
				BigDecimal fixedMindue = loan.getPrincipal().multiply(monthlyRate).multiply(powR)
						.divide(powR.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
				loan.setFixedMindue(fixedMindue);
				loan.setAccountId(transaction.getAccountId());
				loanRepository.save(loan);
			});
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(request.getResultsMap())
				.build());
        responseObserver.onCompleted();
	}
	@Override
	public void initialBillingCycle(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Long accountId = Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8());
		LocalDate businessDate = LocalDate.parse(request.getParamsOrThrow("businessDate").toStringUtf8());
		Loan loan = loanRepository.findByAccountId(accountId);
		Period termsRemaining = loan.getInceptionDate().until(loan.getInceptionDate().plusMonths(loan.getTermMonths()));
		BillingCyclePosting billingCycle = BillingCyclePosting.builder().periodStartDate(loan.getInceptionDate())
				.periodEndDate(loan.getInceptionDate().plusMonths(1).minusDays(1)).fixedMindue(loan.getFixedMindue())
				.deliquent(Boolean.FALSE)
				.closed(Boolean.FALSE)
				.termsRemaining(termsRemaining.getMonths())
				.mindueDate(loan.getInceptionDate().plusMonths(1).minusDays(1).minusDays(5)).principal(loan.getPrincipal())
				.build();
		BillingCycle billingCycleTransaction = BillingCycle.builder()
				// TODO: redo the table id to be sequence id.
				.accountId(loan.getAccountId())
				.version(1L)
				.businessDate(businessDate)
				.transactionDate(billingCycle.retrieveTransactionDate())
				.periodEndDate(billingCycle.getPeriodEndDate())
				.transactionType(TransactionType.BILLING_CYCLE)
				.payload(postingReader.writeValueAsString(billingCycle)).build();
		billingCycleRepository.save(billingCycleTransaction);
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(request.getResultsMap())
				.build());
        responseObserver.onCompleted();
	}
}