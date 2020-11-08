package com.github.karlnicholas.djsdist.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Observable;
import java.util.concurrent.ThreadLocalRandom;

import com.github.karlnicholas.djsdist.client.NotificationParameters.ACCOUNT_ACTIONS;
import com.github.karlnicholas.djsdist.model.Account;

public class RandomizedTestAccount extends AccountAndBillingCycle implements AccountHolder {
//	private int paymentCount;
	private Boolean paymentMade;
	private LocalDate openDate;

	public RandomizedTestAccount(LocalDate openDate) {
		this.openDate = openDate;
	}
	@Override
	public void update(Observable o, Object arg) {
		NotificationParameters notificationParameters = (NotificationParameters)arg;
		LocalDate businessDate = notificationParameters.getDate();
		// check openAccount first
		if ( notificationParameters.getAction() == ACCOUNT_ACTIONS.OPEN_ACCOUNT && openDate.isEqual(businessDate) ) {
			notificationParameters.getAccountHandler().createAccount(openDate, 12, new BigDecimal("10000.00"), new BigDecimal("0.0699"), this);
	//		paymentCount = 0;
			paymentMade = Boolean.FALSE;
		} else if ( notificationParameters.getAction() == ACCOUNT_ACTIONS.BUSINESS_DATE
				&& (billingCycle.getPeriodStartDate().plusDays(3).compareTo(businessDate) <= 0 || billingCycle.getDeliquent())
				&& !paymentMade.booleanValue()
		) {
//				System.out.println("parameters =" + parameters + ":" + billingCyclePosting);
//			if ( parameters.getAction() == ACCOUNT_ACTIONS.PAYMENT && !paymentMade.booleanValue() ) {
//				String minusBillingDate = billingCyclePosting.getPeriodEndDate().toString();
//				String businessDate = parameters.getDate().toString();
			int daysDiff = Math.abs(Period.between(billingCycle.getPeriodEndDate(), businessDate).getDays());
			double divisor = daysDiff;
			double nextG = ThreadLocalRandom.current().nextDouble();
			if ( !billingCycle.getDeliquent() ) {
				divisor = (daysDiff/2.0)*(daysDiff/2.0);
			}
			double val = 3/((double)(5+divisor));
			boolean chance = nextG < val;
//System.out.println(businessDate +":" + minusBillingDate +":"+val+":"+nextG+":"+daysDiff+":"+chance);
			if ( chance ) {
				notificationParameters.getAccountHandler().getStatement(this);
				LocalDate paymentDate = minusDaysRandom(businessDate, 5);
				notificationParameters.getAccountHandler().makePayment(account, billingCycle.getFixedMindue(), businessDate, paymentDate);
				paymentMade = Boolean.TRUE;
			}
		} else if ( notificationParameters.getAction() == ACCOUNT_ACTIONS.BUSINESS_DATE
				&& (billingCycle.getPeriodEndDate().compareTo(businessDate) == 0)
				&& paymentMade.booleanValue()
		) {
			paymentMade = Boolean.FALSE;
		} else if ( notificationParameters.getAction() == ACCOUNT_ACTIONS.BUSINESS_DATE
				&& (billingCycle.getPeriodEndDate().compareTo(businessDate) == 0)
				&& !paymentMade.booleanValue()
		) {
			billingCycle.setDeliquent(Boolean.TRUE);
		}
	}


	public LocalDate plusOrMinusWeighted(LocalDate date, int days) {
		long daysOffset = Math.min(days, Math.max(-days, (long)(ThreadLocalRandom.current().nextGaussian()*(double)days)));
		return date.plusDays(daysOffset);
	}

	public LocalDate minusDaysRandom(LocalDate date, int days) {
		long daysOffset = (long)(ThreadLocalRandom.current().nextDouble() * (double)days);
		return date.minusDays(daysOffset);
	}
	public Account getAccount() {
		return account;
	}

}

/*
} else if ( notificationParameters.getAction() == ACCOUNT_ACTIONS.PAYMENT 
&& billingCycle.getMindueDate().isEqual(businessDate)
&& false
) {
paymentMade = Boolean.TRUE;
notificationParameters.getAccountHandler().makePayment(account, billingCycle.getFixedMindue(), businessDate, businessDate);
*/
