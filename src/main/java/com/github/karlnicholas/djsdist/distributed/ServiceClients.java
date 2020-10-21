package com.github.karlnicholas.djsdist.distributed;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.github.karlnicholas.djsdist.distributed.TransactionProcessorGrpc.TransactionProcessorBlockingStub;
import com.github.karlnicholas.djsdist.distributed.BillingCycleProcessorGrpc.BillingCycleProcessorBlockingStub;
import com.github.karlnicholas.djsdist.distributed.Grpcservices.WorkItemMessage;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Component
public class ServiceClients {
	private TransactionProcessorBlockingStub transactionProcessorBlockingStub;
	private BillingCycleProcessorBlockingStub billingCycleBlockingStub;
	
	@PostConstruct
	public void init() {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress("localhost", 6565);
        channelBuilder.usePlaintext();
        ManagedChannel channel =  channelBuilder.build();
        transactionProcessorBlockingStub = TransactionProcessorGrpc.newBlockingStub(channel);
    	billingCycleBlockingStub = BillingCycleProcessorGrpc.newBlockingStub(channel);
	}


    public WorkItemMessage validateAndProcessTransaction(WorkItemMessage wim) {
		return transactionProcessorBlockingStub.validateAndProcess(wim);
    }
    
    public WorkItemMessage accountFunded(WorkItemMessage wim) {
		return transactionProcessorBlockingStub.accountFunded(wim);
    }

    public WorkItemMessage initialBillingCycle(WorkItemMessage wim) {
		return transactionProcessorBlockingStub.initialBillingCycle(wim);
    }


	public WorkItemMessage accountDueDate(WorkItemMessage wim) {
		return billingCycleBlockingStub.accountDueDate(wim);
    }

	public WorkItemMessage accountInterest(WorkItemMessage wim) {
		return billingCycleBlockingStub.accountInterest(wim);
    }

	public WorkItemMessage accountBillingCycle(WorkItemMessage wim) {
		return billingCycleBlockingStub.accountBillingCycle(wim);
    }

	public WorkItemMessage accountClosing(WorkItemMessage wim) {
		return billingCycleBlockingStub.accountClosing(wim);
    }

}
