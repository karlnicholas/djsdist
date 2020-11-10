package com.github.karlnicholas.djsdist.message;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsFoundMessage implements Serializable {
	private static final long serialVersionUID = -1L;
	private LocalDate date;
	private Boolean transactionsFound;
}
