package com.github.karlnicholas.djsdist.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.github.karlnicholas.djsdist.model.BillingCycle;
import com.github.karlnicholas.djsdist.model.TransactionType;

public interface BillingCycleRepository extends CrudRepository<BillingCycle, Long>  {
	List<BillingCycle> findByAccountId(Long id);
	Optional<BillingCycle> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);
	
	@Query(value = "select b.* from billing_cycle b where b.account_id = :accountId and b.id = (select max(id) from billing_cycle where account_id = :accountId )")
	BillingCycle fetchLatestBillingCycleForAccount(@Param("accountId") Long accountId);

	@Query(value = "select b.* from billing_cycle b where b.id not in (select distinct(t1.id) from billing_cycle t1, billing_cycle t2 where t1.account_id = t2.account_id and t1.transaction_type = t2.transaction_type and t1.business_date < t2.business_date)")
	List<BillingCycle> fetchLatestBillingCycles();

	@Query(value = "select b.* from billing_cycle b where b.transaction_date = :transactionDate and b.id  = (select max(id) from billing_cycle where transaction_date = :transactionDate)")
	List<BillingCycle> fetchBillingCyclesForDate(@Param("transactionDate") LocalDate transactionDate);
	@Query(value = "select b.* from billing_cycle b where b.period_end_date = :periodEndDate and b.id  = (select max(id) from billing_cycle where period_end_date = :periodEndDate)")
	List<BillingCycle> fetchBillingCyclesForPeriodEndDate(@Param("periodEndDate")LocalDate periodEndDate);

	List<BillingCycle> findAllByOrderByTransactionDate();

	List<BillingCycle> findByAccountIdOrderById(Long id);
	BillingCycle getBillingCycleByAccountIdAndPeriodEndDate(Long accountId, LocalDate periodEndDate);
}
