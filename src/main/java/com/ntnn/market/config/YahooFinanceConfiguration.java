package com.ntnn.market.config;

import com.ntnn.market.yahoo.YahooFinanceClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YahooFinanceConfiguration {
    @Bean
    YahooFinanceClient yahooFinanceClient() {
        return new YahooFinanceClient();
    }
}
