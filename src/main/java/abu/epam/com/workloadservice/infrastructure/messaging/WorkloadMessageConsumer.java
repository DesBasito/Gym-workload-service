package abu.epam.com.workloadservice.infrastructure.messaging;

import abu.epam.com.workloadservice.config.RabbitMQConfig;
import abu.epam.com.workloadservice.domain.dto.WorkloadRequest;
import abu.epam.com.workloadservice.domain.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageConsumer {

    private final WorkloadService workloadService;

    @RabbitListener(queues = RabbitMQConfig.WORKLOAD_QUEUE)
    public void receiveWorkloadMessage(WorkloadRequest request,
                                       @Header(name = "transactionId", required = false) String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put("transactionId", transactionId);

        try {
            log.info("Received workload message from RabbitMQ. Action: {}, Trainer: {}",
                    request.getActionType(), request.getUsername());

            validateRequest(request);
            workloadService.processWorkload(request);

            log.info("Workload message processed successfully for trainer: {}", request.getUsername());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid workload message rejected. Reason: {}. Trainer: {}",
                    ex.getMessage(), request.getUsername());
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to process workload message for trainer: {}. Error: {}",
                    request.getUsername(), ex.getMessage());
            throw ex;
        } finally {
            MDC.remove("transactionId");
        }
    }

    private void validateRequest(WorkloadRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (request.getTrainingDuration() == null || request.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration must be positive");
        }
        if (request.getActionType() == null) {
            throw new IllegalArgumentException("Action type is required");
        }
    }
}