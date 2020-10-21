package com.github.karlnicholas.djsdist.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.karlnicholas.djsdist.journal.InterestPosting;
import com.github.karlnicholas.djsdist.journal.LateFeePosting;
import com.github.karlnicholas.djsdist.journal.PaymentCreditPosting;
import com.github.karlnicholas.djsdist.journal.PaymentDebitPosting;
import com.github.karlnicholas.djsdist.model.Account;
import com.github.karlnicholas.djsdist.model.AccountClosed;
import com.github.karlnicholas.djsdist.model.Loan;
import com.github.karlnicholas.djsdist.model.LoanClosed;
import com.github.karlnicholas.djsdist.model.TransactionClosed;
import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.repository.AccountClosedRepository;
import com.github.karlnicholas.djsdist.repository.AccountRepository;
import com.github.karlnicholas.djsdist.repository.LoanClosedRepository;
import com.github.karlnicholas.djsdist.repository.LoanRepository;
import com.github.karlnicholas.djsdist.repository.TransactionClosedRepository;
import com.github.karlnicholas.djsdist.repository.TransactionOpenRepository;

@Service
public class AccountClosedService {
	private final AccountRepository accountRepository;
	private final LoanRepository loanRepository;
	private final LoanClosedRepository loanClosedRepository;
	private final AccountClosedRepository accountClosedRepository;
	private final PostingReader postingReader;
	private final TransactionOpenRepository transactionOpenRepository;
	private final TransactionClosedRepository transactionClosedRepository;
	
	public AccountClosedService(
		AccountRepository accountRepository, 
		LoanRepository loanRepository, 
		LoanClosedRepository loanClosedRepository, 
		AccountClosedRepository accountClosedRepository, 
		PostingReader postingReader, 
		TransactionOpenRepository transactionOpenRepository, 
		TransactionClosedRepository transactionClosedRepository
	) {
		this.accountRepository = accountRepository; 
		this.loanRepository = loanRepository; 
		this.loanClosedRepository = loanClosedRepository; 
		this.accountClosedRepository = accountClosedRepository; 
		this.postingReader = postingReader;
		this.transactionOpenRepository = transactionOpenRepository; 
		this.transactionClosedRepository = transactionClosedRepository;
	}

	@Transactional
	public void closeAccount(Long accountId) {
		Account account = accountRepository.findById(accountId).orElseThrow(()->new IllegalStateException("Account not found: " + accountId));
		AccountClosed accountClosed = accountClosedRepository.save(AccountClosed.builder().originalId(account.getId()).openDate(account.getOpenDate()).build());
		Loan loan = loanRepository.findByAccountId(account.getId());
		loanRepository.delete(loan);
		loanClosedRepository.save(
			LoanClosed.builder()
				.accountClosedId(accountClosed.getId())
				.inceptionDate(loan.getInceptionDate())
				.interestRate(loan.getInterestRate())
				.principal(loan.getPrincipal())
				.termMonths(loan.getTermMonths())
				.build());
		List<TransactionOpen> alltra = transactionOpenRepository.findByAccountIdOrderById(account.getId());
		alltra.forEach(transaction->{
			transactionOpenRepository.delete(transaction);
			transactionClosedRepository.save(
					TransactionClosed.builder()
					.version(transaction.getVersion())
					.accountClosedId(accountClosed.getId())
					.businessDate(transaction.getBusinessDate())
					.transactionDate(transaction.getTransactionDate())
					.payload(transaction.getPayload())
					.transactionType(transaction.getTransactionType())
					.build()
				);
		});
		accountRepository.delete(account);
	}

	public AccountClosedSummary getAccountClosedSummary(Long originalId) {
		Optional<AccountClosed> account = accountClosedRepository.findByOriginalId(originalId);
		if ( account.isPresent() ) {
			AccountClosedSummary accountSummary = AccountClosedSummary.builder()
					.account(account.get())
					.loans(loanClosedRepository.findByAccountClosedId(account.get().getId()).get().getPrincipal())
					.payments(BigDecimal.ZERO)
					.latefees(BigDecimal.ZERO)
					.interest(BigDecimal.ZERO)
					.paymentCount(Integer.valueOf(0))
					.billingCycles(Integer.valueOf(0))
					.build();
			transactionClosedRepository.findByAccountClosedId(account.get().getId()).forEach(transaction->{
				switch (transaction.getTransactionType()) {
				case BILLING_CYCLE:
					accountSummary.setBillingCycles(accountSummary.getBillingCycles()+1);
					break;
				case PAYMENT_DEBIT:
					PaymentDebitPosting accountDebitPosting = postingReader.readValue(transaction, PaymentDebitPosting.class);
					accountSummary.setPayments(accountSummary.getPayments().subtract(accountDebitPosting.getAmount()));
					break;
				case PAYMENT_CREDIT:
					PaymentCreditPosting accountCreditPosting = postingReader.readValue(transaction, PaymentCreditPosting.class);
					accountSummary.setPayments(accountSummary.getPayments().add(accountCreditPosting.getAmount()));
					accountSummary.setPaymentCount(accountSummary.getPaymentCount()+1);
					break;
				case LATE_FEE_DEBIT:
					LateFeePosting lateFeeDebitPosting = postingReader.readValue(transaction, LateFeePosting.class);
					accountSummary.setLatefees(accountSummary.getLatefees().add(lateFeeDebitPosting.getAmount()));
					break;
				case LATE_FEE_CREDIT:
					LateFeePosting lateFeeCreditPosting = postingReader.readValue(transaction, LateFeePosting.class);
					accountSummary.setLatefees(accountSummary.getLatefees().subtract(lateFeeCreditPosting.getAmount()));
					break;
				case INTEREST_DEBIT:
					InterestPosting interestDebitPosting = postingReader.readValue(transaction, InterestPosting.class);
					accountSummary.setInterest(accountSummary.getInterest().add(interestDebitPosting.getAmount()));
					break;
				case INTEREST_CREDIT:
					InterestPosting interestCreditPosting = postingReader.readValue(transaction, InterestPosting.class);
					accountSummary.setInterest(accountSummary.getInterest().subtract(interestCreditPosting.getAmount()));
					break;
				default:
					break;
				}
			});
			return accountSummary;
		} else {
			throw new IllegalArgumentException("No Account Found: " + originalId);
		}
	}
/*
	private AccountSummary getAccountSummary(Long accountId) {
		Optional<Account> account = accountClosedRepository.findById(accountId);
		if ( account.isPresent() ) {
			AccountSummary accountSummary = AccountSummary.builder()
					.account(account.get())
					.loans(loanRepository.findByAccountId(accountId).getPrincipal())
					.payments(BigDecimal.ZERO)
					.latefees(BigDecimal.ZERO)
					.interest(BigDecimal.ZERO)
					.build();
			transactionOpenRepository.findByAccountId(accountId).forEach(transaction->{
				switch (transaction.getType()) {
				case PAYMENT_DEBIT:
					PaymentDebitPosting accountDebitPosting = postingReader.readValue(transaction, PaymentDebitPosting.class);
					accountSummary.setPayments(accountSummary.getPayments().subtract(accountDebitPosting.getAmount()));
					break;
				case PAYMENT_CREDIT:
					PaymentCreditPosting accountCreditPosting = postingReader.readValue(transaction,
							PaymentCreditPosting.class);
					accountSummary.setPayments(accountSummary.getPayments().add(accountCreditPosting.getAmount()));
					break;
				case LATE_FEE_DEBIT:
					LateFeePosting lateFeeDebitPosting = postingReader.readValue(transaction, LateFeePosting.class);
					accountSummary.setLatefees(accountSummary.getLatefees().add(lateFeeDebitPosting.getAmount()));
					break;
				case LATE_FEE_CREDIT:
					LateFeePosting lateFeeCreditPosting = postingReader.readValue(transaction, LateFeePosting.class);
					accountSummary.setLatefees(accountSummary.getLatefees().subtract(lateFeeCreditPosting.getAmount()));
					break;
				case INTEREST_DEBIT:
					InterestPosting interestDebitPosting = postingReader.readValue(transaction, InterestPosting.class);
					accountSummary.setInterest(accountSummary.getInterest().add(interestDebitPosting.getAmount()));
					break;
				case INTEREST_CREDIT:
					InterestPosting interestCreditPosting = postingReader.readValue(transaction, InterestPosting.class);
					accountSummary.setInterest(accountSummary.getInterest().subtract(interestCreditPosting.getAmount()));
					break;
				default:
					break;
				}
			});
			return accountSummary;
		} else {
			throw new IllegalArgumentException("No Account Found: " + accountId);
		}
	}
*/
	public Iterable<AccountClosed> accountsClosed() {
		return accountClosedRepository.findAll();
	}

	public List<TransactionClosed> getTransactionsClosed(Long accountId) {
		return transactionClosedRepository.findByAccountClosedId(accountId);
	}
}
