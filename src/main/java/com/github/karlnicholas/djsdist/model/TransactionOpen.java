package com.github.karlnicholas.djsdist.model;

import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionOpen extends Transaction {
	private Long accountId;
    @Override
    @Transient
    public boolean isNew() {
        return this.isNewTransaction() || getId() == null;
    }

    public TransactionOpen setAsNew() {
        this.setNewTransaction(true);
        return this;
    }
}
