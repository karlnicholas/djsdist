package com.github.karlnicholas.djsdist.distributed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.lognet.springboot.grpc.GRpcService;

import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;
import com.github.karlnicholas.djsdist.handler.AccountBalances;
import com.github.karlnicholas.djsdist.journal.BillingCyclePosting;
import com.github.karlnicholas.djsdist.journal.InterestPosting;
import com.github.karlnicholas.djsdist.journal.LateFeePosting;
import com.github.karlnicholas.djsdist.journal.LoanFundingPosting;
import com.github.karlnicholas.djsdist.journal.PaymentCreditPosting;
import com.github.karlnicholas.djsdist.journal.PaymentDebitPosting;
import com.github.karlnicholas.djsdist.model.Account;
import com.github.karlnicholas.djsdist.model.Loan;
import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.github.karlnicholas.djsdist.repository.AccountRepository;
import com.github.karlnicholas.djsdist.repository.LoanRepository;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;
import com.github.karlnicholas.djsdist.service.AccountClosedService;
import com.github.karlnicholas.djsdist.service.BusinessDateService;
import com.github.karlnicholas.djsdist.service.PostingReader;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

@GRpcService
public class BillingCycleProcessor extends BillingCycleProcessorGrpc.BillingCycleProcessorImplBase {
	
	private final PostingReader postingReader;
	private final AccountRepository accountRepository;
	private final TransactionOpenRepository transactionOpenRepository;
	private final LoanRepository loanRepository;
	private final BusinessDateService businessDateService;
	private final AccountClosedService accountClosedService;

	public BillingCycleProcessor(
			PostingReader postingReader, 
			AccountRepository accountRepository, 
			TransactionOpenRepository transactionOpenRepository, 
			LoanRepository loanRepository, 
			BusinessDateService businessDateService, 
			AccountClosedService accountClosedService
	) {
		this.accountRepository = accountRepository;
		this.postingReader = postingReader;
		this.transactionOpenRepository = transactionOpenRepository;
		this.loanRepository = loanRepository;
		this.businessDateService = businessDateService;
		this.accountClosedService = accountClosedService;
	}
	@Override
	public void accountDueDate(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Map<String, ByteString> results = new HashMap<>();
		results.putAll(request.getResultsMap());
		Long accountId = Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8());
		String billingDate = request.getParamsOrThrow("billingdate").toStringUtf8();
		Optional<Account> account = accountRepository.findById(accountId);
		if (account.isPresent()) {
			BillingCyclePosting billingCyclePosting = getBillingCycleByDateOrLatest(account.get().getId(), billingDate);
			BigDecimal lateFeeAmount = updateLateFeeForBillingCycle(account.get(), billingCyclePosting);
			if ( lateFeeAmount.compareTo(BigDecimal.ZERO) > 0 ) {
				results.put("LateFee", ByteString.copyFromUtf8(lateFeeAmount.toString()));
			}
		} else {
			throw new IllegalArgumentException("invalid accountId in accountDueDate: " + accountId);
		}
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(results)
				.build());
        responseObserver.onCompleted();
    }


	private BigDecimal updateLateFeeForBillingCycle(Account account, BillingCyclePosting billingCyclePosting) {
		AccountBalances accountBalances = getAccountBalances(account.getId(), billingCyclePosting);
		BigDecimal mindueRemaing = billingCyclePosting.getFixedMindue().subtract(accountBalances.getDueDateCredits());
		BigDecimal lateFeeDue = BigDecimal.ZERO;
		if (mindueRemaing.compareTo(BigDecimal.ZERO) > 0 && billingCyclePosting.getPrincipal().subtract(accountBalances.getTotalCredits()).compareTo(BigDecimal.ZERO) > 0 ) {
			lateFeeDue = new BigDecimal("39.00");
		}
		// compare to existing latefee amount
		lateFeeDue = lateFeeDue.subtract(accountBalances.getLateFee());
		// get existing lateFee journal entry, if any
		if ( lateFeeDue.compareTo(BigDecimal.ZERO) != 0) {
			TransactionType lateFeeType = TransactionType.LATE_FEE_DEBIT;
			if ( lateFeeDue.compareTo(BigDecimal.ZERO) < 0 ) {
				lateFeeType = TransactionType.LATE_FEE_CREDIT;
				lateFeeDue = lateFeeDue.abs();
			}
			persistLateFeeRecord(account, billingCyclePosting, lateFeeDue, lateFeeType);
		}
		return lateFeeDue;
	}

	private void persistLateFeeRecord(Account account, BillingCyclePosting billingCyclePosting, BigDecimal amount, TransactionType lateFeeType) {
		// create latefee debit
		LateFeePosting lateFeePosting = LateFeePosting.builder()
				.amount(amount).date(billingCyclePosting.getMindueDate())
				.build();

		TransactionOpen lateFeeTransaction = TransactionOpen.builder()
				.accountId(account.getId())
				.version(1L)
				.businessDate(businessDateService.getBusinessDate())
				.transactionDate(billingCyclePosting.retrieveTransactionDate())
				.transactionType(lateFeeType)
				.payload(postingReader.writeValueAsString(lateFeePosting))
				.build();
		transactionOpenRepository.save(lateFeeTransaction);
	}

	@Override
	public void accountInterest(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Map<String, ByteString> results = new HashMap<>();
		results.putAll(request.getResultsMap());
		Long accountId = Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8());
		String billingDate = request.getParamsOrThrow("billingdate").toStringUtf8();
		Optional<Account> account = accountRepository.findById(accountId);
		if (account.isPresent()) {
			BillingCyclePosting billingCyclePosting = getBillingCycleByDateOrLatest(account.get().getId(), billingDate);
			BigDecimal interestDue = updateInterestForBillingCycle(Account.builder().id(accountId).build(), billingCyclePosting);
			results.put("Interest", ByteString.copyFromUtf8(interestDue.toString()));
		} else {
			throw new IllegalArgumentException("invalid accountId in accountInterest: " + accountId);
		}
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(results)
				.build());
        responseObserver.onCompleted();
	}

	private BigDecimal updateInterestForBillingCycle(Account account, BillingCyclePosting billingCyclePosting) {
		Optional<TransactionOpen> fundingTransaction = transactionOpenRepository.findByAccountIdAndTransactionType(account.getId(), TransactionType.LOAN_FUNDING);
		if (fundingTransaction.isPresent()) {
			LoanFundingPosting loanFundPosting = postingReader.readValue(fundingTransaction.get(), LoanFundingPosting.class);

			AccountBalances accountBalances  = getAccountBalances(account.getId(), billingCyclePosting);
	
			BigDecimal principal = billingCyclePosting.getPrincipal().add(accountBalances.getTotalDebits()).subtract(accountBalances.getTotalCredits());
			BigDecimal interestDue = BigDecimal.ZERO;
			if (principal.compareTo(BigDecimal.ZERO) > 0) {
				// interest = pr * rate * fraction
				interestDue = principal.multiply(loanFundPosting.getInterestRate())
						.divide(BigDecimal.valueOf(loanFundPosting.getTermMonths()), 2, RoundingMode.UP);
			}
			// compare to existing interest amount
			interestDue = interestDue.subtract(accountBalances.getInterest());
			if ( interestDue.compareTo(BigDecimal.ZERO) != 0) {
				TransactionType interestType = TransactionType.INTEREST_DEBIT;
				if ( interestDue.compareTo(BigDecimal.ZERO) < 0 ) {
					interestType = TransactionType.INTEREST_CREDIT;
					interestDue = interestDue.abs();
				}
				persistInterestRecord(fundingTransaction.get(), billingCyclePosting, interestDue, interestType);
			}
			return interestDue;
		}
		return BigDecimal.ZERO;
	}
	
	private void persistInterestRecord(TransactionOpen fundingTransaction, BillingCyclePosting billingCyclePosting, BigDecimal interestDue, TransactionType transactionType) {
		// create interest debit
		InterestPosting interestPosting = InterestPosting.builder().amount(interestDue)
				.date(billingCyclePosting.getPeriodEndDate().minusDays(1)).build();

		TransactionOpen interestTransaction = TransactionOpen.builder()
				.accountId(fundingTransaction.getAccountId()).version(1L)
				.businessDate(businessDateService.getBusinessDate())
				.transactionDate(interestPosting.retrieveTransactionDate())
				.transactionType(transactionType)
				.payload(postingReader.writeValueAsString(interestPosting)).build();

		transactionOpenRepository.save(interestTransaction);
	}

	@Override
	public void accountBillingCycle(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Long accountId = Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8());
		String billingDate = request.getParamsOrThrow("billingdate").toStringUtf8();
		Optional<Account> account = accountRepository.findById(accountId);
		if (account.isPresent()) {
			BillingCyclePosting billingCyclePosting = getBillingCycleByDateOrLatest(accountId, billingDate);
			billingCycleForBillingCycle(account.get(), billingCyclePosting);
		} else {
			throw new IllegalArgumentException("invalid accountId in accountBillingCycle: " + accountId);
		}
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(request.getResultsMap())
				.build());
        responseObserver.onCompleted();
    }

	private BillingCyclePosting billingCycleForBillingCycle(Account account, BillingCyclePosting billingCyclePosting) {
		Loan loan = loanRepository.findByAccountId(account.getId());
		AccountBalances accountBalances  = getAccountBalances(account.getId(), billingCyclePosting);
		BigDecimal newPrincipal = accountBalances.getPrincipal().add(accountBalances.getTotalDebits()).subtract(accountBalances.getTotalCredits());
		
		if (billingCyclePosting.getPrincipal().compareTo(BigDecimal.ZERO) < 0) {
			billingCyclePosting.setPrincipal(BigDecimal.ZERO);
		}
		Period termsRemaining = billingCyclePosting.getPeriodEndDate().until(loan.getInceptionDate().plusMonths(loan.getTermMonths()));
		
		Boolean deliquent = Boolean.FALSE;
		BigDecimal deliquentAmount = BigDecimal.ZERO.max(billingCyclePosting.getFixedMindue().subtract(accountBalances.getTotalCredits()));
		if ( deliquentAmount.compareTo(BigDecimal.ZERO) > 0 )  {
			deliquent = Boolean.TRUE;
		}
		BigDecimal newMindue = newPrincipal.min(loan.getFixedMindue().add(deliquentAmount).add(accountBalances.getLateFee()));
		if ( newPrincipal.compareTo(BigDecimal.ZERO) <= 0 || termsRemaining.getMonths() <= -3) {
			billingCyclePosting = BillingCyclePosting.builder()
					.fixedMindue(newMindue)
					.periodStartDate(billingCyclePosting.getPeriodEndDate())
					.periodEndDate(billingCyclePosting.getPeriodEndDate())
					.mindueDate(billingCyclePosting.getPeriodEndDate())
					.deliquent(deliquent)
					.closed(Boolean.TRUE)
					.termsRemaining(termsRemaining.getMonths())
					.principal(newPrincipal).build();
		} else {
			billingCyclePosting = BillingCyclePosting.builder()
					.fixedMindue(newMindue)
					.periodStartDate(billingCyclePosting.getPeriodEndDate().plusDays(1))
					.periodEndDate(billingCyclePosting.getPeriodEndDate().plusMonths(1))
					.mindueDate(billingCyclePosting.getPeriodEndDate().plusMonths(1).minusDays(5))
					.deliquent(deliquent)
					.closed(Boolean.FALSE)
					.termsRemaining(termsRemaining.getMonths())
					.principal(newPrincipal).build();
		}

		TransactionOpen billingCycleTransaction = TransactionOpen.builder()
				.accountId(account.getId())
				.version(1L)
				.businessDate(businessDateService.getBusinessDate())
				.transactionDate(billingCyclePosting.retrieveTransactionDate())
				.transactionType(TransactionType.BILLING_CYCLE)
				.payload(postingReader.writeValueAsString(billingCyclePosting)).build();

		return postingReader.readValue(transactionOpenRepository.save(billingCycleTransaction), BillingCyclePosting.class);
	}

	@Override
	public void accountClosing(WorkItemMessage request, StreamObserver<WorkItemMessage> responseObserver) {
		Long accountId = Long.parseLong(request.getParamsOrThrow("subject").toStringUtf8());
		String billingDate = request.getParamsOrThrow("billingdate").toStringUtf8();
		Optional<Account> account = accountRepository.findById(accountId);
		if (account.isPresent()) {
			BillingCyclePosting billingCyclePosting = getBillingCycleByDateOrLatest(accountId, billingDate);
			accountClosingForBillingCycle(account.get(), billingCyclePosting);
		} else {
			throw new IllegalArgumentException("invalid accountId in accountInterest: " + accountId);
		}
		responseObserver.onNext(request.toBuilder()
				.putAllParams(request.getParamsMap())
				.putAllResults(request.getResultsMap())
				.build());
        responseObserver.onCompleted();
    }

	private void accountClosingForBillingCycle(Account account, BillingCyclePosting billingCyclePosting) {
		if (billingCyclePosting.getClosed().booleanValue()) {
			accountClosedService.closeAccount(account.getId());
		}
	}

	private AccountBalances getAccountBalances(Long accountId, BillingCyclePosting billingCycle) {
		AccountBalances accountBalances = new AccountBalances();
		accountBalances.setPrincipal(billingCycle.getPrincipal());
		accountBalances.setTotalDebits(BigDecimal.ZERO);
		accountBalances.setTotalCredits(BigDecimal.ZERO);
		accountBalances.setDueDateDebits(BigDecimal.ZERO);
		accountBalances.setDueDateCredits(BigDecimal.ZERO);
		accountBalances.setLateFee(BigDecimal.ZERO);
		accountBalances.setInterest(BigDecimal.ZERO);
		List<TransactionOpen> transactions = getTransactionsForBillingCycle(accountId, billingCycle);
		// determine billing cycle period
		for (Transaction transaction : transactions) {
			switch (transaction.getTransactionType()) {
			case PAYMENT_DEBIT:
				PaymentDebitPosting paymentDebitPosting = postingReader.readValue(transaction, PaymentDebitPosting.class);
				accountBalances.setTotalDebits(accountBalances.getTotalDebits().add(paymentDebitPosting.getAmount()));
				if ( paymentDebitPosting.retrieveTransactionDate().compareTo(billingCycle.getMindueDate()) <= 0 ) {
					accountBalances.setDueDateDebits(accountBalances.getDueDateDebits().add(paymentDebitPosting.getAmount()));
				}
				break;
			case PAYMENT_CREDIT:
				PaymentCreditPosting paymentCreditPosting = postingReader.readValue(transaction, PaymentCreditPosting.class);
				accountBalances.setTotalCredits(accountBalances.getTotalCredits().add(paymentCreditPosting.getAmount()));
				if ( paymentCreditPosting.retrieveTransactionDate().compareTo(billingCycle.getMindueDate()) <= 0 ) {
					accountBalances.setDueDateCredits(accountBalances.getDueDateCredits().add(paymentCreditPosting.getAmount()));
				}
				break;
			case LATE_FEE_DEBIT:
				LateFeePosting lateFeeDebitPosting = postingReader.readValue(transaction, LateFeePosting.class);
				accountBalances.setTotalDebits(accountBalances.getTotalDebits().add(lateFeeDebitPosting.getAmount()));
				accountBalances.setLateFee(accountBalances.getLateFee().add(lateFeeDebitPosting.getAmount()));
				break;
			case LATE_FEE_CREDIT:
				LateFeePosting lateFeeCreditPosting = postingReader.readValue(transaction, LateFeePosting.class);
				accountBalances.setTotalCredits(accountBalances.getTotalCredits().add(lateFeeCreditPosting.getAmount()));
				accountBalances.setLateFee(accountBalances.getLateFee().subtract(lateFeeCreditPosting.getAmount()));
				break;
			case INTEREST_DEBIT:
				InterestPosting interestDebitPosting = postingReader.readValue(transaction, InterestPosting.class);
				accountBalances.setTotalDebits(accountBalances.getTotalDebits().add(interestDebitPosting.getAmount()));
				accountBalances.setInterest(accountBalances.getInterest().add(interestDebitPosting.getAmount()));
				break;
			case INTEREST_CREDIT:
				InterestPosting interestCreditPosting = postingReader.readValue(transaction, InterestPosting.class);
				accountBalances.setTotalDebits(accountBalances.getTotalDebits().subtract(interestCreditPosting.getAmount()));
				accountBalances.setInterest(accountBalances.getInterest().subtract(interestCreditPosting.getAmount()));
				break;
			default:
				break;
			}
		}
		return accountBalances;
	}
	private List<TransactionOpen> getTransactionsForBillingCycle(Long accountId, BillingCyclePosting billingCycle) {
		List<TransactionOpen> transactions = transactionOpenRepository.findByAccountId(accountId);
		return transactions.stream()
				.filter(transaction -> postingReader.instancePayload(transaction).inBillingCycle(billingCycle))
				.collect(Collectors.toList());
	}

	private BillingCyclePosting getBillingCycleByDateOrLatest(Long accountId, String billingDate) {
		TransactionOpen transaction = transactionOpenRepository.fetchLatestBillingCycleForAccount(accountId);
		return postingReader.readValue(transaction, BillingCyclePosting.class);
	}

}
