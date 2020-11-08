package com.github.karlnicholas.djsdist.client;

import java.time.LocalDate;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NotificationParameters {
	public enum ACCOUNT_ACTIONS {OPEN_ACCOUNT, BUSINESS_DATE};
	private ACCOUNT_ACTIONS action;
	private LocalDate date;
	private AccountHandler accountHandler;
}
