package com.github.karlnicholas.djsdist.model;

import java.time.LocalDate;

import org.springframework.data.domain.Persistable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BillingCycle extends TransactionOpen implements Persistable<Long> {
	private LocalDate periodEndDate;
}
