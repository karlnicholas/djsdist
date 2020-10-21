package com.github.karlnicholas.djsdist.model;

import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionSubmitted extends Transaction {
	private Long accountId;
    @Transient
    public boolean isNew() {
        return this.isNewTransaction() || getId() == null;
    }

    public TransactionSubmitted setAsNew() {
        this.setNewTransaction(true);
        return this;
    }

}
