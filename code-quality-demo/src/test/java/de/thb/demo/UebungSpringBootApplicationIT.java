package de.thb.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Endung *IT: wird von Failsafe als Integrationstest ausgeführt (siehe pom.xml)
@SpringBootTest
class UebungSpringBootApplicationIT {

	@Test
	void contextLoads() {
		System.out.println("This is a integration test!");
	}

}
