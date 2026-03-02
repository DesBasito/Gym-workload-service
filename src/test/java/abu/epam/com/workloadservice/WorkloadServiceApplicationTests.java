package abu.epam.com.workloadservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@ActiveProfiles("test")
class WorkloadServiceApplicationTests {

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
    }

}