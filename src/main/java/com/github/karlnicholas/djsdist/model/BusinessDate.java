package com.github.karlnicholas.djsdist.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BusinessDate {
	@Id private Long id;
	private LocalDate businessDate;
	private LocalDate processDate;
}
