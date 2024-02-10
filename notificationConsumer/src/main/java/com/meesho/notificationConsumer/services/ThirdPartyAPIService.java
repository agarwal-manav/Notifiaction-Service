package com.meesho.notificationConsumer.services;

import com.meesho.notificationConsumer.constants.Constants;
import com.meesho.notificationConsumer.dto.request.thirdPatyAPI.RequestBodyChannel;
import com.meesho.notificationConsumer.dto.request.thirdPatyAPI.RequestBodyDestination;
import com.meesho.notificationConsumer.dto.request.thirdPatyAPI.RequestBodyTemplate;
import com.meesho.notificationConsumer.dto.request.thirdPatyAPI.RequestChannelSms;
import com.meesho.notificationConsumer.models.RequestDatabase;
import com.meesho.notificationConsumer.repository.RequestDatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Service
public class ThirdPartyAPIService {

    @Autowired
    private RequestDatabaseRepository requestDatabaseRepository;

    @Autowired
    private RestTemplate restTemplate;

    Logger logger = LoggerFactory.getLogger(ThirdPartyAPIService.class);

    public Boolean sendSMSAPI(RequestDatabase requestData){
        try {
            String API_KEY = Constants.THIRD_PARTY_API_KEY;
            String API_URL = Constants.THIRD_PARTY_API;

            HttpHeaders requestHeaders   = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            requestHeaders.set("key", API_KEY);

            RequestBodyTemplate requestBody = requestBodyBuilder(requestData);

            HttpEntity<RequestBodyTemplate> request = new HttpEntity<>(requestBody, requestHeaders);
            Object response = restTemplate.postForObject(API_URL, request, Object.class);

            logger.info("Response From 3rd Party Api : {}", response);

            return Boolean.TRUE;

        } catch (Error err){
            logger.error(err.getMessage());
            return Boolean.FALSE;

        } catch (Exception ex){
            logger.error(ex.getMessage());
            return Boolean.FALSE;
        }
    }

    private RequestBodyTemplate requestBodyBuilder(RequestDatabase requestData) {

        RequestChannelSms requestChannelSms = new RequestChannelSms(requestData.getMessage());
        RequestBodyChannel requestChannel   = new RequestBodyChannel(requestChannelSms);

        ArrayList<String> destinationContacts = new ArrayList<>();
        destinationContacts.add(String.format("+91" + requestData.getPhoneNumber()));

        RequestBodyDestination requestDestination = RequestBodyDestination.builder()
                .correlationid(requestData.getRequestId())
                .msisdn(destinationContacts)
                .build();

        ArrayList<RequestBodyDestination> requestDestinations = new ArrayList<>();
        requestDestinations.add(requestDestination);

        RequestBodyTemplate requestBody = RequestBodyTemplate.builder()
                .deliverychannel("sms")
                .channels(requestChannel)
                .destination(requestDestinations)
                .build();

        logger.info("delivery Message Object Creation Done {}", requestBody);
        return requestBody;

    }
}