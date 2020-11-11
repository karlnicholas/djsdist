package com.github.karlnicholas.djsdist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.model.TransactionType;

public interface TransactionOpenRepository extends CrudRepository<TransactionOpen, Long>  {
	List<TransactionOpen> findByAccountId(Long id);
	Optional<TransactionOpen> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);
	
	List<TransactionOpen> findAllByOrderByTransactionDate();

	List<TransactionOpen> findByAccountIdOrderById(Long id);
}
