package abu.epam.com.workloadservice.domain.service;

import abu.epam.com.workloadservice.domain.dto.WorkloadRequest;
import abu.epam.com.workloadservice.domain.model.TrainerWorkload;
import abu.epam.com.workloadservice.domain.port.WorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("WorkloadService Unit Tests")
@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private WorkloadRepository workloadRepository;

    @InjectMocks
    private WorkloadService workloadService;

    @Captor
    private ArgumentCaptor<TrainerWorkload> workloadCaptor;

    private Map<String, TrainerWorkload> testStorage;

    @BeforeEach
    void setUp() {
        reset(workloadRepository);
        testStorage = new HashMap<>();

        lenient().when(workloadRepository.findByUsername(anyString())).thenAnswer(inv -> {
            String username = inv.getArgument(0);
            return Optional.ofNullable(testStorage.get(username));
        });

        lenient().doAnswer(inv -> {
            TrainerWorkload workload = inv.getArgument(0);
            testStorage.put(workload.getUsername(), workload);
            return null;
        }).when(workloadRepository).save(any(TrainerWorkload.class));
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

        verify(workloadRepository).save(workloadCaptor.capture());
        TrainerWorkload saved = workloadCaptor.getValue();

        assertNotNull(saved);
        assertEquals("john.doe", saved.getUsername());
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertTrue(saved.getIsActive());
        assertEquals(60, saved.getTotalDuration(2026, 2));
    }

    @Test
    @DisplayName("Should accumulate training duration for multiple ADD operations")
    void testProcessWorkload_MultipleAdd() {
        String username = "jane.smith";

        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 10), 90, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 15), 60, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 3, 20), 45, WorkloadRequest.ActionType.ADD));

        verify(workloadRepository, times(3)).save(workloadCaptor.capture());
        TrainerWorkload finalWorkload = testStorage.get(username);
        assertEquals(195, finalWorkload.getTotalDuration(2026, 3));
    }

    @Test
    @DisplayName("Should subtract training duration when action type is DELETE")
    void testProcessWorkload_Delete() {
        String username = "bob.trainer";

        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 4, 5), 120, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 4, 5), 60, WorkloadRequest.ActionType.DELETE));

        verify(workloadRepository, times(2)).save(workloadCaptor.capture());
        TrainerWorkload finalWorkload = testStorage.get(username);
        assertEquals(60, finalWorkload.getTotalDuration(2026, 4));
    }

    @Test
    @DisplayName("Should not allow negative duration (Math.max protection)")
    void testProcessWorkload_DeleteMoreThanAvailable() {
        String username = "test.trainer";

        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 5, 10), 30, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 5, 10), 100, WorkloadRequest.ActionType.DELETE));

        verify(workloadRepository, times(2)).save(workloadCaptor.capture());
        TrainerWorkload finalWorkload = testStorage.get(username);
        assertEquals(0, finalWorkload.getTotalDuration(2026, 5));
    }

    @Test
    @DisplayName("Should handle multiple months and years correctly")
    void testProcessWorkload_MultipleMonthsAndYears() {
        String username = "multi.trainer";

        workloadService.processWorkload(createRequest(username, LocalDate.of(2025, 12, 20), 60, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 1, 10), 90, WorkloadRequest.ActionType.ADD));
        workloadService.processWorkload(createRequest(username, LocalDate.of(2026, 2, 15), 45, WorkloadRequest.ActionType.ADD));

        verify(workloadRepository, times(3)).save(workloadCaptor.capture());
        TrainerWorkload finalWorkload = testStorage.get(username);
        assertNotNull(finalWorkload);
        assertEquals(60, finalWorkload.getTotalDuration(2025, 12));
        assertEquals(90, finalWorkload.getTotalDuration(2026, 1));
        assertEquals(45, finalWorkload.getTotalDuration(2026, 2));
    }

    @Test
    @DisplayName("Should get all workloads correctly")
    void testGetAllWorkloads() {
        TrainerWorkload workload1 = TrainerWorkload.builder()
                .username("trainer1")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        TrainerWorkload workload2 = TrainerWorkload.builder()
                .username("trainer2")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        TrainerWorkload workload3 = TrainerWorkload.builder()
                .username("trainer3")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();

        Map<String, TrainerWorkload> mockWorkloads = Map.of(
                "trainer1", workload1,
                "trainer2", workload2,
                "trainer3", workload3
        );

        when(workloadRepository.findAll()).thenReturn(mockWorkloads);

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

        WorkloadRequest request2 = WorkloadRequest.builder()
                .username(username)
                .firstName("New")
                .lastName("UpdatedName")
                .isActive(false)
                .trainingDate(LocalDate.of(2026, 1, 2))
                .trainingDuration(30)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        workloadService.processWorkload(request1);
        workloadService.processWorkload(request2);

        verify(workloadRepository, times(2)).save(workloadCaptor.capture());
        TrainerWorkload finalWorkload = testStorage.get(username);

        assertEquals("New", finalWorkload.getFirstName());
        assertEquals("UpdatedName", finalWorkload.getLastName());
        assertFalse(finalWorkload.getIsActive());
        assertEquals(90, finalWorkload.getTotalDuration(2026, 1));
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
