package com.meesho.notificationProducer.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class AddOrRemoveBlacklistResponse extends ResponseObject {
    private String comments;
}
