package com.github.karlnicholas.djsdist.service;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.ServerRequest;

import com.github.karlnicholas.djsdist.model.Account;
import com.github.karlnicholas.djsdist.repository.AccountRepository;

@Service
public class AccountService {
	private final AccountRepository accountRepository;
	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}
	public void createAccount(ServerRequest serverRequest) {
			try {
				accountRepository.save(serverRequest.body(Account.class));
			} catch (ServletException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
//		queueNewTransactionPost(transactionSubmitted.getAccount().getId(), transactionSubmitted.getId(), "transaction");

	}
	public void queryAccount(ServerRequest serverRequest) {
		accountRepository.findById(Long.valueOf(serverRequest.pathVariable("id")));
	}
}
