package com.github.karlnicholas.djsdist.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class Transaction implements Persistable<Long> {
	@Id private Long id;

	private Long version;
	private LocalDate businessDate;
	private LocalDate transactionDate;
	private TransactionType transactionType;
	
	private String payload;
    @Transient
    private boolean newTransaction;

/*    
    @Override
    public String toString() { 
    	return "Transaction [id="+id+", type="+type+", date="+date+", payload="+payload+"]";
    }
*/
}
