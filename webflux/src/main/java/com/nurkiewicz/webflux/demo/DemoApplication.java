package com.nurkiewicz.webflux.demo;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		BlockHound.install();
		Schedulers.enableMetrics();
		InitDocker.start().block(Duration.ofMinutes(2));
		SpringApplication.run(DemoApplication.class, args);
	}

}
