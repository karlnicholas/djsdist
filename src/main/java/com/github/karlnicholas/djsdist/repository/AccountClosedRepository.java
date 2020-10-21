package com.github.karlnicholas.djsdist.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.github.karlnicholas.djsdist.model.AccountClosed;

public interface AccountClosedRepository extends CrudRepository<AccountClosed, Long>{

	Optional<AccountClosed> findByOriginalId(Long originalId);

}
