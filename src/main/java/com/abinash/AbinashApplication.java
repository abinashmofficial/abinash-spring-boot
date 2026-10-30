package com.abinash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AbinashApplication {

	public static void main(String[] args) {
		Thread.currentThread().setName("abinash");
		SpringApplication.run(AbinashApplication.class, args);
	}

}
