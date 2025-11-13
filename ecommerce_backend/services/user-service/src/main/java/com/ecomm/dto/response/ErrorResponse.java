package com.ecomm.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ErrorResponse {

    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private String correlationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Singular
    private List<FieldErrorItem> fieldErrors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldErrorItem {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}

