package com.meesho.notificationProducer.controllers;

import com.meesho.notificationProducer.constants.Constants;
import com.meesho.notificationProducer.dto.request.AddOrRemoveBlacklistRequest;
import com.meesho.notificationProducer.dto.request.CheckIfBlacklistedRequest;
import com.meesho.notificationProducer.dto.response.*;
import com.meesho.notificationProducer.services.storageServices.RedisCacheServices;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/blacklist")
public class BlacklistController {

    @Autowired
    private RedisCacheServices redisServices;

    private final static Logger logger = LoggerFactory.getLogger(BlacklistController.class);

    @PostMapping
    public ResponseEntity<ResponseObject> addPhoneNumbersToBlacklist(@Valid @RequestBody AddOrRemoveBlacklistRequest blacklistRequest) {

        try{
            List<String> phoneNumbersToAdd = blacklistRequest.getPhoneNumbers();
            logger.info("Adding Phone numbers to Redis cache");

            redisServices.addNumbersToBlacklist(phoneNumbersToAdd);

            logger.info("Added Phone Numbers to Cache Successfully!!");

            AddOrRemoveBlacklistResponse response = AddOrRemoveBlacklistResponse.builder()
                    .comments(Constants.NUMBERS_ADDED_TO_BLACKLIST)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Error error) {

            logger.error(error.toString());
            ErrorResponse response = ErrorResponse.builder()
                    .errorComment(error.toString())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    public ResponseEntity<ResponseObject> removePhoneNumbersFromBlacklist(@Valid @RequestBody AddOrRemoveBlacklistRequest blacklistRequest) {

        try{
            List<String> phoneNumbersToRemove = blacklistRequest.getPhoneNumbers();
            logger.info("Removing Phone Numbers from Cache");

            redisServices.removeNumbersFromBlacklist(phoneNumbersToRemove);

            logger.info("Removed Phone Numbers from Cache Successfully!!");

            AddOrRemoveBlacklistResponse response = AddOrRemoveBlacklistResponse.builder()
                    .comments(Constants.NUMBERS_DELETED_FROM_BLACKLIST)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Error error){
            logger.error(error.toString());
            ErrorResponse response = ErrorResponse.builder()
                    .errorComment(error.toString())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseObject> fetchPhoneNumberBlacklist() {
        try{
            List<String> fetchedBlacklist = redisServices.getAllBlacklistedNumbers();

            logger.info("fetched Blacklist : {}", fetchedBlacklist);

            FetchBlacklistResponse response = FetchBlacklistResponse.builder()
                    .comments("Fetched Successfully!")
                    .blacklist(fetchedBlacklist)
                    .build();


            return ResponseEntity.ok(response);
        }
        catch (Error error) {
            logger.error(error.toString());
            ErrorResponse response = ErrorResponse.builder()
                    .errorComment(error.toString())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ResponseObject> checkPhoneNumberIfExists(@Valid @RequestBody CheckIfBlacklistedRequest checkIfBlacklistedRequest) {
        try{
            String phoneNumber = checkIfBlacklistedRequest.getPhoneNumber();
            CheckIfBlacklistedResponse response = new CheckIfBlacklistedResponse(redisServices.checkIfBlacklisted(phoneNumber));

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Error error) {
            logger.error(error.toString());
            ErrorResponse response = ErrorResponse.builder()
                    .errorComment(error.toString())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
