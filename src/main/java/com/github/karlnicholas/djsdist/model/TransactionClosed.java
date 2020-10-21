package com.github.karlnicholas.djsdist.model;

import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransactionClosed extends Transaction {
	private Long accountClosedId;
    @Transient
    public boolean isNew() {
        return this.isNewTransaction() || getId() == null;
    }

    public TransactionClosed setAsNew() {
        this.setNewTransaction(true);
        return this;
    }
}
