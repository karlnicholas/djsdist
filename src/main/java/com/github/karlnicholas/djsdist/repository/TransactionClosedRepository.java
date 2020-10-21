package com.github.karlnicholas.djsdist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.github.karlnicholas.djsdist.model.TransactionClosed;
import com.github.karlnicholas.djsdist.model.TransactionType;

public interface TransactionClosedRepository extends CrudRepository<TransactionClosed, Long> {

	List<TransactionClosed> findByAccountClosedId(Long id);

	Optional<TransactionClosed> findByTransactionTypeAndAccountClosedId(TransactionType transactionType, Long fromString);

	@Query(value = "select t.* from transaction_closed t join account_closed ac where t.account_closed_id = ac.id and ac.original_id = :accountClosedId and t.transaction_type = 'BILLING_CYCLE' and t.id = (select max(t2.id) from transaction_closed t2 join account_closed ac2 where t2.account_closed_id = ac2.id and ac2.original_id = :accountClosedId and t2.transaction_type = 'BILLING_CYCLE')")
	TransactionClosed fetchLatestBillingCycleForAccount(@Param("accountClosedId") Long accountClosedId);

}
