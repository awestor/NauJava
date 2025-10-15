package ru.daniil.NauJava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NauJavaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context =
				SpringApplication.run(NauJavaApplication.class, args);
		SpringApplication.exit(context, () -> 0);
	}

}
