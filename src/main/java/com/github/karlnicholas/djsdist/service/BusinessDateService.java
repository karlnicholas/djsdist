package com.github.karlnicholas.djsdist.service;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class BusinessDateService {
	private LocalDate businessDate;
	
	@PostConstruct
	public void postConstruct() {
		businessDate = LocalDate.now();
	}
	
	public LocalDate getBusinessDate() {
		return businessDate;
	}
	public void setBusinessDate(LocalDate businessDate) {
		this.businessDate = businessDate;
	}
}
