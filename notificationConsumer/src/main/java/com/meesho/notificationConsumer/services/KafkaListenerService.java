package com.meesho.notificationConsumer.services;

import com.meesho.notificationConsumer.constants.Constants;
import com.meesho.notificationConsumer.models.ESDocument;
import com.meesho.notificationConsumer.models.RequestDatabase;
import com.meesho.notificationConsumer.repository.RequestDatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class KafkaListenerService {

    @Autowired
    private RequestDatabaseRepository databaseRepository;
    @Autowired
    private SQLDatabaseServices       sqlDatabaseServices;
    @Autowired
    private RedisCacheServices        redisCacheServices;
    @Autowired
    private ThirdPartyAPIService      thirdPartyAPIServices;
    @Autowired
    private ElasticSearchServices     ESServices;

    private final Logger logger          = LoggerFactory.getLogger(KafkaListenerService.class);
    private static final String TOPIC    = Constants.KAFKA_TOPIC;
    private static final String GROUP_ID = Constants.KAFKA_GROUP;

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeFromTopic(String requestId) throws IOException {

        try{

            logger.info("Consuming message with id: \"{}\"", requestId);

            /* Step 1 : Get details From Database With the Request ID */
            Optional<RequestDatabase> optionalRequestDatabase = databaseRepository.findById(requestId);
            RequestDatabase requestData;

            if(optionalRequestDatabase.isPresent()){
                requestData = optionalRequestDatabase.get();
                logger.info("Consuming Sms Request Details : {}", requestData);
            } else {
                throw new Error(String.format("Consuming Request Details is not found with request ID : %s ", requestId));
            }

            /* Step 2 : Check Number is blacklisted via Redis */
            if(Boolean.TRUE.equals(redisCacheServices.checkIfBlacklisted(requestData.getPhoneNumber()))){
                throw new Error(String.format("Consumer Phone Number (%s) is blacklisted", requestData.getPhoneNumber()));
            }

            /* Step 3 : Call the 3rd Party API for sending message */
            if(Boolean.TRUE.equals(thirdPartyAPIServices.sendSMSAPI(requestData))){
                logger.info("Successfully sent the message using Third Party API");

                if(Boolean.FALSE.equals(sqlDatabaseServices.updateStatusOnSuccess(requestId))) {
                    throw new Error(String.format("Error while updating status for request Id : %s", requestId));
                }

            } else {
                throw new Error(String.format("Error while sending SMS from Third party API with request Id : %s", requestId));
            }

            /* Step 4 : Index the message content in the ES */
            ESDocument esDocument = ESDocument.builder()
                    .id(requestId)
                    .createdAt(new Date())
                    .message(requestData.getMessage())
                    .phoneNumber(requestData.getPhoneNumber())
                    .build();

            if(Boolean.TRUE.equals(ESServices.indexToElasticSearchDB(esDocument))) {
                logger.info("Successfully indexed message to Elastic Search");
            } else {
                throw new Error(String.format("Error while indexing message to Elastic Search with request Id : %s", requestId));
            }

        } catch (Error error) {

            logger.error(error.getMessage());

            if(Boolean.FALSE.equals(sqlDatabaseServices.updateStatusOnFailure(requestId, error.getMessage()))) {
                logger.error("Error while updating status for request Id : {}", requestId);
            }

        }
    }
}