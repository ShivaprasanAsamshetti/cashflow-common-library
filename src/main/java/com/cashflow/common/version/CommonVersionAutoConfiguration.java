package com.cashflow.common.version;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonVersionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VersionController.class)
    public VersionController versionController() {
        return new VersionController();
    }
}
