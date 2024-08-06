package com.egnamespace.eventing.egeventing;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.namespaces.EventGridSenderClient;
import com.azure.messaging.eventgrid.namespaces.EventGridSenderClientBuilder;

@SpringBootApplication
public class EgeventingApplication {
	
	public static final String ENDPOINT =  "ADD-YOUR-ENDPOINT-HERE";
	public static final int NUMBER_OF_EVENTS_TO_BUILD_THAT_DOES_NOT_EXCEED_100 = 10;

    private static final String TOPIC_NAME = "ADD-YOUR-TOPIC-NAME-HERE";
    public static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential("ADD-YOUR-KEY-HERE");
    
	public static void main(String[] args) {
		SpringApplication.run(EgeventingApplication.class, args);

        EventGridSenderClient client = new EventGridSenderClientBuilder().endpoint(ENDPOINT)
            .topicName(TOPIC_NAME)
            .credential(CREDENTIAL)
            .buildClient();
	
            Payload user = new Payload("Parameters", "{ \"maxSize\": \"100\", \"defaultPageNumbers\": \"10\" }", new java.sql.Date(System.currentTimeMillis()));
            
            // for loop to send 10 messages 
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_BUILD_THAT_DOES_NOT_EXCEED_100; i++) {
                CloudEvent cloudEvent
                    = new CloudEvent("source", "type", BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");
                client.send(cloudEvent);
         
                System.out.println("Event sent" + i);

            }
           
	}

	
}
