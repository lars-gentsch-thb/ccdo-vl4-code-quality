package de.thb.demo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

// Endung *Tests: wird von Surefire als Unit-Test ausgeführt (siehe pom.xml)
// Spring Boot 4: @SpringBootTest stellt MockMvc nur noch mit @AutoConfigureMockMvc bereit
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void testIndex() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from")));
    }

    @Test
    void testCreateLogs() throws Exception {
        mvc.perform(get("/something"))
                .andExpect(status().isOk())
                .andExpect(content().string("All Ok"));
    }
}
