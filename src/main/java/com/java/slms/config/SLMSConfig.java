package com.java.slms.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SLMSConfig
{
    @Bean
    public ModelMapper modelMapper()
    {
        return new ModelMapper();
    }
}
