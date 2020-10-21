package com.github.karlnicholas.djsdist.model;


import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

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
public class AccountClosed implements Persistable<Long> {
	@Id private Long id;

	private LocalDate openDate;
	private Long originalId;

	@Transient
    private boolean newAccountClosed;

    @Override
    @Transient
    public boolean isNew() {
        return this.newAccountClosed || id == null;
    }

    public AccountClosed setAsNew() {
        this.newAccountClosed = true;
        return this;
    }

}
