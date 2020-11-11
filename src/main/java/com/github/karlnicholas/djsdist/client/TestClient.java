package com.github.karlnicholas.djsdist.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;

import com.github.karlnicholas.djsdist.client.NotificationParameters.ACCOUNT_ACTIONS;
import com.github.karlnicholas.djsdist.model.Account;
import com.github.karlnicholas.djsdist.service.AccountClosedSummary;
import com.github.karlnicholas.djsdist.service.PostingReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestClient extends Observable {
	private ObjectMapperWrapper objectMapper = new ObjectMapperWrapper(new ObjectMapper());
	private RestTemplate restTemplate = new RestTemplate();
	private PostingReader postingReader = new PostingReader();

	public static void main(String[] args) throws Exception {
		new TestClient().run();
	}
	public TestClient() {
		objectMapper = new ObjectMapperWrapper(new ObjectMapper());
		restTemplate = new RestTemplate();
		postingReader = new PostingReader();
		objectMapper.getObjectMapper().registerModule(new JavaTimeModule());
		objectMapper.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		postingReader.setObjectMapper(objectMapper.getObjectMapper());
	}

	boolean accountCreated = false;


	private void run() throws Exception {
		List<AccountHandler> accounts = new ArrayList<>();
/*		
		DataDrivenTestAccounts testAccounts = objectMapper.readValue(this.getClass().getResourceAsStream("/testdata/testaccounts.json"), DataDrivenTestAccounts.class);
		for ( DataDrivenTestAccount dataDrivenTestAccount: testAccounts.getTestAccounts() ) {
			if ( !dataDrivenTestAccount.getDisabled().booleanValue() ) {
				AccountHandler accountHandler = new AccountHandler(restTemplate, objectMapper, postingReader);
				dataDrivenTestAccount.initialize();
				accountHandler.addAccount(dataDrivenTestAccount);
				addObserver(accountHandler);
				accounts.add(accountHandler);
			}
		}
*/
		cycleForTwoYearsYear(accounts);
		log.debug("Number of accounts: {}", accounts.size());
		log.debug("Account List: {}", accounts.stream().map(a->a.getAccount().toString()).collect(Collectors.toList()));
		for(AccountHandler accountHandler: accounts) {
			log.debug("AccountSummary: {} ", getAccountClosedSummary(restTemplate, accountHandler.getAccount()));
		}
	}
	public void cycleForTwoYearsYear(List<AccountHandler> accounts) throws Exception {
		LocalDate runningDate = LocalDate.of(2020, 1, 1);
		NotificationParameters notificationParameters = new NotificationParameters();
		while ( runningDate.isBefore(LocalDate.of(2022, 1, 1))) {
			notificationParameters.setDate(runningDate);
			postBusinessDate(runningDate);
			setChanged();
			notifyObservers(notificationParameters.setAction(ACCOUNT_ACTIONS.BUSINESS_DATE));
//			postBillingCycle(runningDate);
			boolean newAccount;
			do {
				newAccount = false;
				if ( runningDate.isBefore(LocalDate.of(2020, 6, 1)) && ThreadLocalRandom.current().nextDouble() > .80  && accountCreated == false) {
accountCreated = true;
					RandomizedTestAccount randomizedTestAccount = new RandomizedTestAccount(runningDate);
					AccountHandler accountHandler = new AccountHandler(restTemplate, objectMapper, postingReader);
					accountHandler.addAccount(randomizedTestAccount);
					addObserver(accountHandler);
					accounts.add(accountHandler);
					newAccount = true;
				}
			} while ( newAccount );
			setChanged();
			notifyObservers(notificationParameters.setAction(ACCOUNT_ACTIONS.OPEN_ACCOUNT));
			runningDate = runningDate.plusDays(1);
		}
	}
	private void postBusinessDate(LocalDate businessDate) throws Exception {
		restTemplate.postForLocation("http://localhost:8080/businessdate/{businessDate}", null, businessDate);
	}
/*
	private Long[] postBillingCycle(LocalDate cycleDate) throws Exception {
		Long[] results = restTemplate.getForEntity("http://localhost:8080/billingcycle", Long[].class).getBody();
	//System.out.println("cycleDate:" + cycleDate +":results:" + results.length);
		return results;
	}
*/
	private AccountClosedSummary getAccountClosedSummary(RestTemplate restTemplate, Account account) {
		return restTemplate.getForEntity("http://localhost:8080/queue/get/accountclosedsummary/{accountId}", AccountClosedSummary.class, account.getId()).getBody();
	}

}

/*		
new Thread(()->{
	try {
		makeAccount();
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}) .start();
new Thread(()->{
	try {
		makeAccount();
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}) .start();
*/
