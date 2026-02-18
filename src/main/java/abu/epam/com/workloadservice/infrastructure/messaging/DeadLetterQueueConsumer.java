package abu.epam.com.workloadservice.infrastructure.messaging;

import abu.epam.com.workloadservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterQueueConsumer {

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void processDeadLetterMessage(Message message) {
        log.error("Received message in Dead Letter Queue. MessageId: {}, Body: {}",
                message.getMessageProperties().getMessageId(),
                new String(message.getBody()));

        log.error("Original exchange: {}, Original routing key: {}, Headers: {}",
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(),
                message.getMessageProperties().getHeaders());
    }
}