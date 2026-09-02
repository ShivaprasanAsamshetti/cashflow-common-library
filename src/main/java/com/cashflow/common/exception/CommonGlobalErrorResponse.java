package com.cashflow.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonGlobalErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);

    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private List<FieldErrorDetail> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
