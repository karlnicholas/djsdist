package com.github.karlnicholas.djsdist.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.Transaction;

public interface TransactionRejectedRepository extends CrudRepository<Transaction, Long> {
	@Query("select * from transaction_rejected")
	List<Transaction> findAll();
}
