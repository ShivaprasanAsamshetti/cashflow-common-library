package com.cashflow.common.exception;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommonGlobalExceptionHandler.class)
    public CommonGlobalExceptionHandler commonGlobalExceptionHandler() {
        return new CommonGlobalExceptionHandler();
    }
}
