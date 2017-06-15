package org.rutebanken.irkalla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IrkallaApplication {

	public static void main(String[] args) {
		SpringApplication.run(IrkallaApplication.class, args);
	}
}
