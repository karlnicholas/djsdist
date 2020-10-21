package com.github.karlnicholas.djsdist.repository;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.Account;

public interface AccountRepository extends CrudRepository<Account, Long>  {
}
