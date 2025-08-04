package com.java.slms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class SlmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlmsApplication.class, args);
	}

}
