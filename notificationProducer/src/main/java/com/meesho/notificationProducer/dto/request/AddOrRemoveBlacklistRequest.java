package com.meesho.notificationProducer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meesho.notificationProducer.constants.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddOrRemoveBlacklistRequest {

    @JsonProperty("phone_numbers")
    @NotEmpty(message = Constants.EMPTY_PHONE_NUMBER_LIST)

    List< @NotBlank
            @Pattern(regexp = Constants.PHONE_NUMBER_REGEX, message = Constants.INVAlID_PHONE_NUMBER)
            String> phoneNumbers;
}
