package com.emiLoan.EMILoan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EmiLoanApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmiLoanApplication.class, args);
	}

}
