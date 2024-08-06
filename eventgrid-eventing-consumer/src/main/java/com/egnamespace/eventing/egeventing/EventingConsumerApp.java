package com.egnamespace.eventing.egeventing;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.namespaces.EventGridReceiverClient;
import com.azure.messaging.eventgrid.namespaces.EventGridReceiverClientBuilder;
import com.azure.messaging.eventgrid.namespaces.EventGridServiceVersion;
import com.azure.messaging.eventgrid.namespaces.models.AcknowledgeResult;
import com.azure.messaging.eventgrid.namespaces.models.BrokerProperties;
import com.azure.messaging.eventgrid.namespaces.models.FailedLockToken;
import com.azure.messaging.eventgrid.namespaces.models.ReceiveDetails;
import com.azure.messaging.eventgrid.namespaces.models.RejectResult;
import com.azure.messaging.eventgrid.namespaces.models.ReleaseResult;

@SpringBootApplication
public class EventingConsumerApp {
	
	public static final String ENDPOINT =  "ADD-YOUR-ENDPOINT-HERE";
	public static final int NUMBER_OF_EVENTS_TO_BUILD_THAT_DOES_NOT_EXCEED_100 = 10;
   

    public static final int MAX_NUMBER_OF_EVENTS_TO_RECEIVE = 10;
    public static final Duration MAX_WAIT_TIME_FOR_EVENTS = Duration.ofSeconds(10);

    private static EventGridReceiverClient eventGridClient;
    private static List<String> receivedCloudEventLockTokens = new ArrayList<>();
    private static List<CloudEvent> receivedCloudEvents = new ArrayList<>();


    private static final String TOPIC_NAME = "ADD-YOUR-TOPIC-NAME-HERE";
    public static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential("ADD-YOUR-KEY-HERE");
    public static final String EVENT_SUBSCRIPTION_NAME = "ADD-YOUR-SUBSCRIPTION-NAME-HERE";


	public static void main(String[] args) {
		SpringApplication.run(EventingConsumerApp.class, args);

        eventGridClient = new EventGridReceiverClientBuilder()
                .httpClient(HttpClient.createDefault())  
                .endpoint(ENDPOINT)
                .topicName(TOPIC_NAME)
                .subscriptionName(EVENT_SUBSCRIPTION_NAME)
                .serviceVersion(EventGridServiceVersion.V2024_06_01)
                .credential(CREDENTIAL).buildClient();   

        System.out.println("Waiting " +  MAX_WAIT_TIME_FOR_EVENTS.toSecondsPart() + " seconds for events to be read...");

        List<ReceiveDetails> receiveDetails = eventGridClient.receive(MAX_NUMBER_OF_EVENTS_TO_RECEIVE, MAX_WAIT_TIME_FOR_EVENTS).getDetails();

        for (ReceiveDetails detail : receiveDetails) {
            // Add order message received to a tracking list
            CloudEvent orderCloudEvent = detail.getEvent();
            System.out.println("Received event: " + orderCloudEvent.getData());

            receivedCloudEvents.add(orderCloudEvent);
            // Add lock token to a tracking list. Lock token functions like an identifier to a cloudEvent
            BrokerProperties metadataForCloudEventReceived = detail.getBrokerProperties();
            String lockToken = metadataForCloudEventReceived.getLockToken();
            receivedCloudEventLockTokens.add(lockToken);
        }
        acknowledge(receivedCloudEventLockTokens);
      //  release(receivedCloudEventLockTokens);

        System.out.println("<-- Number of events received: " + receivedCloudEvents.size());
	}

	   private static void acknowledge(List<String> lockTokens) {
        AcknowledgeResult acknowledgeResult = eventGridClient.acknowledge(lockTokens);
        List<String> succeededLockTokens = acknowledgeResult.getSucceededLockTokens();
        if (succeededLockTokens != null && lockTokens.size() >= 1)
            System.out.println("@@@ " + succeededLockTokens.size() + " events were successfully acknowledged:");
        for (String lockToken : succeededLockTokens) {
            System.out.println("    Acknowledged event lock token: " + lockToken);
        }
      
        // Print the information about failed lock tokens
        if (succeededLockTokens.size() < lockTokens.size()) {
            System.out.println("    At least one event was not acknowledged (deleted from Event Grid)");
            writeFailedLockTokens(acknowledgeResult.getFailedLockTokens());
            reject(receivedCloudEventLockTokens);
        }
      
    }

    private static void writeFailedLockTokens(List<FailedLockToken> failedLockTokens) {
        for (FailedLockToken failedLockToken : failedLockTokens) {
            System.out.println("    Failed lock token: " + failedLockToken.getLockToken());
            System.out.println("    Error code: " + failedLockToken.getError().getCode());
            System.out.println("    Error description: " + failedLockToken.getError().getMessage());
        }
        
    }

    private static void release(List<String> lockTokens) {
        ReleaseResult releaseResult = eventGridClient.release(lockTokens);
        List<String> succeededLockTokens = releaseResult.getSucceededLockTokens();
        if (succeededLockTokens != null && lockTokens.size() >= 1)
            System.out.println("^^^ " + succeededLockTokens.size() + " events were successfully released:");
        for (String lockToken : succeededLockTokens) {
            System.out.println("    Released event lock token: " + lockToken);
        }
        // Print the information about failed lock tokens
        if (succeededLockTokens.size() < lockTokens.size()) {
            System.out.println("    At least one event was not released back to Event Grid.");
            writeFailedLockTokens(releaseResult.getFailedLockTokens());
        }
    }
        private static void reject(List<String> lockTokens) {
        RejectResult rejectResult = eventGridClient.reject(lockTokens);
        List<String> succeededLockTokens = rejectResult.getSucceededLockTokens();
        if (succeededLockTokens != null && lockTokens.size() >= 1)
            System.out.println("--- " + succeededLockTokens.size() + " events were successfully rejected:");
        for (String lockToken : succeededLockTokens) {
            System.out.println("    Rejected event lock token: " + lockToken);
        }
        // Print the information about failed lock tokens
        if (succeededLockTokens.size() < lockTokens.size()) {
            System.out.println("    At least one event was not rejected.");
            writeFailedLockTokens(rejectResult.getFailedLockTokens());
        }
    }
}
