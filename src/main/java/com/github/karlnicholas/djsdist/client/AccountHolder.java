package com.github.karlnicholas.djsdist.client;

import java.util.Observer;

import com.github.karlnicholas.djsdist.model.Account;

public interface AccountHolder extends Observer {
	Account getAccount();
}
