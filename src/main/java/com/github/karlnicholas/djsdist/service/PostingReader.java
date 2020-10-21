package com.github.karlnicholas.djsdist.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.karlnicholas.djsdist.journal.BillingCyclePosting;
import com.github.karlnicholas.djsdist.journal.InterestPosting;
import com.github.karlnicholas.djsdist.journal.LateFeePosting;
import com.github.karlnicholas.djsdist.journal.LoanFundingPosting;
import com.github.karlnicholas.djsdist.journal.PaymentCreditPosting;
import com.github.karlnicholas.djsdist.journal.PostingFunctions;
import com.github.karlnicholas.djsdist.model.Transaction;
import com.github.karlnicholas.djsdist.model.TransactionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PostingReader {
	private Map<TransactionType, Class<? extends PostingFunctions>> types;
	{
		types = new HashMap<>();
		types.put(TransactionType.BILLING_CYCLE, BillingCyclePosting.class);
		types.put(TransactionType.INTEREST_CREDIT, InterestPosting.class);
		types.put(TransactionType.INTEREST_DEBIT, InterestPosting.class);
		types.put(TransactionType.LATE_FEE_CREDIT, LateFeePosting.class);
		types.put(TransactionType.LATE_FEE_DEBIT, LateFeePosting.class);
		types.put(TransactionType.LOAN_FUNDING, LoanFundingPosting.class);
		types.put(TransactionType.PAYMENT_CREDIT, PaymentCreditPosting.class);
	}

	@Autowired
	private ObjectMapper objectMapper;
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
    public Class<? extends PostingFunctions> getPostingClassType(Transaction transaction) {
    	return types.get( transaction.getTransactionType());
    }
	
    public <T extends PostingFunctions> T  readValue(Transaction transaction, Class<T> valueType) {
    	try {
			return objectMapper.readValue(transaction.getPayload(), valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
    
    public <T extends PostingFunctions> T instancePayload(Transaction transaction) {
    	try {
    		String canonical = types.get(transaction.getTransactionType()).getCanonicalName();
    		JavaType javaType = objectMapper.getTypeFactory().constructFromCanonical(canonical);
    		return objectMapper.readValue(transaction.getPayload(), javaType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public String writeValueAsString(Object value) {
    	try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
