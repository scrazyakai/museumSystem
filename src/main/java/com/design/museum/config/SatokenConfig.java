package com.design.museum.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SatokenConfig {
    @Bean
    public StpLogic getStpLogicJwt() {
       return new StpLogicJwtForStateless();
    }
}
