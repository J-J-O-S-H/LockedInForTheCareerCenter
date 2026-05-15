package com.lockedin.careercenter.dto;

import java.time.Instant;

public record EventResponse(
        String id,
        String title,
        String location,
        Instant eventDate){

}

