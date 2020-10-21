package com.github.karlnicholas.djsdist.repository;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.Loan;

public interface LoanRepository extends CrudRepository<Loan, Long>  {
	Loan findByAccountId(Long accountId);
}
