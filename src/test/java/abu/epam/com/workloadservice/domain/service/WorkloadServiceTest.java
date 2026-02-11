package abu.epam.com.workloadservice.domain.service;

import abu.epam.com.workloadservice.domain.dto.WorkloadRequest;
import abu.epam.com.workloadservice.domain.model.TrainerWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkloadService Unit Tests")
class WorkloadServiceTest {

    private WorkloadService workloadService;

    @BeforeEach
    void setUp() {
        workloadService = new WorkloadService();
    }

    @Test
    @DisplayName("Should add training duration when action type is ADD")
    void testProcessWorkload_Add() {
        WorkloadRequest request = WorkloadRequest.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 2, 15))
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        workloadService.processWorkload(request);

        TrainerWorkload workload = workloadService.getTrainerWorkload("john.doe");
        assertNotNull(workload);
        assertEquals("john.doe", workload.getUsername());
        assertEquals("John", workload.getFirstName());
        assertEquals("Doe", workload.getLastName());
        assertTrue(workload.getIsActive());
        assertEquals(60, workload.getTotalDuration(2026, 2));
    }

    @Test
    @DisplayName("Should accumulate training duration for multiple ADD operations")
    void testProcessWorkload_MultipleAdd() {
        String username = "jane.smith";
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 10), 90, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 15), 60, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 20), 45, WorkloadRequest.ActionType.ADD));

        TrainerWorkload workload = workloadService.getTrainerWorkload(username);
        assertEquals(195, workload.getTotalDuration(2026, 3));
    }

    @Test
    @DisplayName("Should subtract training duration when action type is DELETE")
    void testProcessWorkload_Delete() {
        String username = "bob.trainer";
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 4, 5), 120, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 4, 5), 60, WorkloadRequest.ActionType.DELETE));

        TrainerWorkload workload = workloadService.getTrainerWorkload(username);
        assertEquals(60, workload.getTotalDuration(2026, 4));
    }

    @Test
    @DisplayName("Should not allow negative duration (Math.max protection)")
    void testProcessWorkload_DeleteMoreThanAvailable() {
        String username = "test.trainer";
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 5, 10), 30, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 5, 10), 100, WorkloadRequest.ActionType.DELETE));

        TrainerWorkload workload = workloadService.getTrainerWorkload(username);
        assertEquals(0, workload.getTotalDuration(2026, 5));
    }

    @Test
    @DisplayName("Should handle multiple months and years correctly")
    void testProcessWorkload_MultipleMonthsAndYears() {
        String username = "multi.trainer";
        workloadService.processWorkload(createRequest(username, LocalDate.of(2025, 12, 20), 60, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 1, 10), 90, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 2, 15), 45, WorkloadRequest.ActionType.ADD));

        TrainerWorkload workload = workloadService.getTrainerWorkload(username);
        assertNotNull(workload);
        assertEquals(60, workload.getTotalDuration(2025, 12));
        assertEquals(90, workload.getTotalDuration(2026, 1));
        assertEquals(45, workload.getTotalDuration(2026, 2));
    }

    @Test
    @DisplayName("Should get all workloads correctly")
    void testGetAllWorkloads() {
        workloadService.processWorkload(createRequest("trainer1", LocalDate.of(2026, 1, 1), 60, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest("trainer2", LocalDate.of(2026, 1, 1), 90, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest("trainer3", LocalDate.of(2026, 1, 1), 45, WorkloadRequest.ActionType.ADD));

        Map<String, TrainerWorkload> allWorkloads = workloadService.getAllWorkloads();
        assertNotNull(allWorkloads);
        assertEquals(3, allWorkloads.size());
        assertTrue(allWorkloads.containsKey("trainer1"));
        assertTrue(allWorkloads.containsKey("trainer2"));
        assertTrue(allWorkloads.containsKey("trainer3"));
    }

    @Test
    @DisplayName("Should return null for non-existent trainer")
    void testGetTrainerWorkload_NotFound() {
        TrainerWorkload workload = workloadService.getTrainerWorkload("non.existent");
        assertNull(workload);
    }

    @Test
    @DisplayName("Should update trainer info on subsequent requests")
    void testProcessWorkload_UpdateTrainerInfo() {
        String username = "update.trainer";
        WorkloadRequest request1 = WorkloadRequest.builder()
                .username(username)
                .firstName("Old")
                .lastName("Name")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 1, 1))
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();
        workloadService.processWorkload(request1);

        WorkloadRequest request2 = WorkloadRequest.builder()
                .username(username)
                .firstName("New")
                .lastName("UpdatedName")
                .isActive(false)
                .trainingDate(LocalDate.of(2026, 1, 2))
                .trainingDuration(30)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();
        workloadService.processWorkload(request2);

        TrainerWorkload workload = workloadService.getTrainerWorkload(username);
        assertEquals("New", workload.getFirstName());
        assertEquals("UpdatedName", workload.getLastName());
        assertFalse(workload.getIsActive());
        assertEquals(90, workload.getTotalDuration(2026, 1));
    }

    private WorkloadRequest createRequest(String username, LocalDate date, int duration, WorkloadRequest.ActionType actionType) {
        return WorkloadRequest.builder()
                .username(username)
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .trainingDate(date)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();
    }
}
