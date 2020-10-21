package com.github.karlnicholas.djsdist.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.github.karlnicholas.djsdist.model.TransactionOpen;
import com.github.karlnicholas.djsdist.model.TransactionType;

public interface TransactionOpenRepository extends CrudRepository<TransactionOpen, Long>  {
	List<TransactionOpen> findByAccountId(Long id);
	Optional<TransactionOpen> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);
	
	@Query(value = "select t.* from transaction_open t where t.account_id = :accountId and t.transaction_type = 'BILLING_CYCLE' and t.id = (select max(id) from transaction_open where account_id = :accountId and transaction_type = 'BILLING_CYCLE')")
	TransactionOpen fetchLatestBillingCycleForAccount(@Param("accountId") Long accountId);

	@Query(value = "select t.* from transaction_open t where t.transaction_type = 'BILLING_CYCLE' and t.id not in (select distinct(t1.id) from transaction_open t1, transaction_open t2 where t1.account_id = t2.account_id and t1.transaction_type = t2.transaction_type and t1.transaction_type = 'BILLING_CYCLE' and t1.business_date < t2.business_date)")
	List<TransactionOpen> fetchLatestBillingCycles();

	@Query(value = "select t.* from transaction_open t where t.transaction_date = :transactionDate and t.transaction_type = 'BILLING_CYCLE' and t.id  = (select max(id) from transaction_open where transaction_type = 'BILLING_CYCLE' and transaction_date = :transactionDate)")
	List<TransactionOpen> fetchBillingCyclesForDate(@Param("transactionDate") LocalDate transactionDate);

	List<TransactionOpen> findAllByOrderByTransactionDate();

	List<TransactionOpen> findByAccountIdOrderById(Long id);
}
