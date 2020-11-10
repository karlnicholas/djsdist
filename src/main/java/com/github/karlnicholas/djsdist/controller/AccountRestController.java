package com.github.karlnicholas.djsdist.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.karlnicholas.djsdist.model.Account;
import com.github.karlnicholas.djsdist.repository.AccountRepository;

@RestController
@RequestMapping("/account")
public class AccountRestController {
//	private static final Logger logger = LoggerFactory.getLogger(AccountRestController.class);
	private final AccountRepository accountRepository;
	public AccountRestController(
			AccountRepository accountRepository 
	) {
		this.accountRepository = accountRepository;
	}
	@PostMapping
	public ResponseEntity<?> createAccount(@RequestBody Account account,  
			HttpServletRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
		try {
			Account newAccount = accountRepository.save(account);
	        UriComponents uriComponents = uriComponentsBuilder.path("/account/{id}").buildAndExpand(newAccount.getId());

	        return ResponseEntity.created(uriComponents.toUri()).body(newAccount);
		} catch ( Exception e ) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

//		queueNewTransactionPost(transactionSubmitted.getAccount().getId(), transactionSubmitted.getId(), "transaction");

	}
	@GetMapping("{accountId}")
	public ResponseEntity<Account> getAccount(@PathVariable("accountId") Long accountId) {
		return ResponseEntity.of(accountRepository.findById(accountId));
	}
	@GetMapping("transactions")
	public ResponseEntity<Iterable<Account>> getAccounts() {
		return ResponseEntity.ok(accountRepository.findAll());
	}
	@GetMapping("count")
	public Long countTransactions() {
		return accountRepository.count();
	}
}
