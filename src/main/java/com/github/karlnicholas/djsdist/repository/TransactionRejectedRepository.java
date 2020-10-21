package com.github.karlnicholas.djsdist.repository;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.Transaction;

public interface TransactionRejectedRepository extends CrudRepository<Transaction	, Long> {}
