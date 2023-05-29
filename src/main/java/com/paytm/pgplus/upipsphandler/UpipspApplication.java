package com.paytm.pgplus.upipsphandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.paytm.pgplus","org.springframework.cache", "software.amazon.awssdk.services"})
public class UpipspApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpipspApplication.class, args);
	}

}
