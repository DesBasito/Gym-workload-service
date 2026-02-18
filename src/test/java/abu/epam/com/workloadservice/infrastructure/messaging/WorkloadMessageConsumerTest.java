package abu.epam.com.workloadservice.infrastructure.messaging;

import abu.epam.com.workloadservice.domain.dto.WorkloadRequest;
import abu.epam.com.workloadservice.domain.service.WorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadMessageConsumer Tests")
class WorkloadMessageConsumerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadMessageConsumer consumer;

    @Test
    @DisplayName("Should process valid workload message with transactionId")
    void testReceiveWorkloadMessage_validRequestWithTransactionId() {
        WorkloadRequest request = buildValidRequest();

        assertDoesNotThrow(() -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verify(workloadService).processWorkload(request);
    }

    @Test
    @DisplayName("Should process valid workload message without transactionId")
    void testReceiveWorkloadMessage_validRequestWithoutTransactionId() {
        WorkloadRequest request = buildValidRequest();

        assertDoesNotThrow(() -> consumer.receiveWorkloadMessage(request, null));

        verify(workloadService).processWorkload(request);
    }

    @Test
    @DisplayName("Should reject message with missing username")
    void testReceiveWorkloadMessage_missingUsername() {
        WorkloadRequest request = buildValidRequest();
        request.setUsername(null);

        assertThrows(IllegalArgumentException.class,
                () -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verifyNoInteractions(workloadService);
    }

    @Test
    @DisplayName("Should reject message with blank username")
    void testReceiveWorkloadMessage_blankUsername() {
        WorkloadRequest request = buildValidRequest();
        request.setUsername("  ");

        assertThrows(IllegalArgumentException.class,
                () -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verifyNoInteractions(workloadService);
    }

    @Test
    @DisplayName("Should reject message with missing training date")
    void testReceiveWorkloadMessage_missingTrainingDate() {
        WorkloadRequest request = buildValidRequest();
        request.setTrainingDate(null);

        assertThrows(IllegalArgumentException.class,
                () -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verifyNoInteractions(workloadService);
    }

    @Test
    @DisplayName("Should reject message with missing action type")
    void testReceiveWorkloadMessage_missingActionType() {
        WorkloadRequest request = buildValidRequest();
        request.setActionType(null);

        assertThrows(IllegalArgumentException.class,
                () -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verifyNoInteractions(workloadService);
    }

    @Test
    @DisplayName("Should reject message with zero training duration")
    void testReceiveWorkloadMessage_zeroTrainingDuration() {
        WorkloadRequest request = buildValidRequest();
        request.setTrainingDuration(0);

        assertThrows(IllegalArgumentException.class,
                () -> consumer.receiveWorkloadMessage(request, "tx-123"));

        verifyNoInteractions(workloadService);
    }

    private WorkloadRequest buildValidRequest() {
        return WorkloadRequest.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 2, 15))
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();
    }
}