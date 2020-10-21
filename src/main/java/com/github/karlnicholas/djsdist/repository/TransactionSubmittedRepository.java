package com.github.karlnicholas.djsdist.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.TransactionSubmitted;

public interface TransactionSubmittedRepository extends CrudRepository<TransactionSubmitted, Long>  {

	List<TransactionSubmitted> findByBusinessDate(LocalDate businessDate);
}
