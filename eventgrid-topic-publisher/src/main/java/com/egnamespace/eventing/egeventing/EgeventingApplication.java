package com.egnamespace.eventing.egeventing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

@SpringBootApplication
public class EgeventingApplication {
	
	public static final String ENDPOINT =  "ADD-ENDPOINT-HERE";
	public static final int NUMBER_OF_EVENTS_TO_BUILD_THAT_DOES_NOT_EXCEED_100 = 10;


    // Event Grid Custom Topic
    public static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential("ADD-AZURE-KEY-HERE");
    
    public static void main(String[] args) {
		SpringApplication.run(EgeventingApplication.class, args);

        EventGridPublisherClient<EventGridEvent> publisherClient = new EventGridPublisherClientBuilder()
        .endpoint(ENDPOINT)  
        .credential(CREDENTIAL)
        .buildEventGridEventPublisherClient();
    
        // Create a CloudEvent with Object data
        Payload payload = new Payload("Parameters", "{ \"maxSize\": \"100\", \"defaultPageNumbers\": \"10\" }", new java.sql.Date(System.currentTimeMillis()));
        for (int i = 0; i < NUMBER_OF_EVENTS_TO_BUILD_THAT_DOES_NOT_EXCEED_100; i++) {
            EventGridEvent event = new EventGridEvent("EG/Topic/Demo", "Parameter.Payload", BinaryData.fromObject(payload), "0.1");

            publisherClient.sendEvent(event);

            System.out.println("Event sent" + i);
        }

        System.out.println("Finished sending events");
           
	}
}
