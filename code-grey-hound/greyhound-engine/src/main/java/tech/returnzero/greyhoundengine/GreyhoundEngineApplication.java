package tech.returnzero.greyhoundengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "tech.returnzero.greyhoundengine")
public class GreyhoundEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreyhoundEngineApplication.class, args);
	}

}
