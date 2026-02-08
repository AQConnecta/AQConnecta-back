package com.aqConnecta.aqConnecta;

import com.aqConnecta.E2ETest;
import com.aqConnecta.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AqConnectaApplicationTests extends E2ETest {
    @MockBean
    private EmailService emailService;

    @Test
    void contextLoads() {
    }

}
