package com.example.help_bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HelpBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpBridgeApplication.class, args);
	}

}
