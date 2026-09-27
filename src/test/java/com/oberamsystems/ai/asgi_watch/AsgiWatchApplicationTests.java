package com.oberamsystems.ai.asgi_watch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class AsgiWatchApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainMethodRuns() {
		assertDoesNotThrow(() -> {
			AsgiWatchApplication.main(new String[]{"--server.port=0", "--spring.main.web-application-type=none"});
		});
	}
}
