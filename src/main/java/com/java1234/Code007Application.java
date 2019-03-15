package com.java1234;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class Code007Application {

	public static void main(String[] args) {
		SpringApplication.run(Code007Application.class, args);
	}
	
}
