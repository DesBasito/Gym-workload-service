package abu.epam.com.workloadservice.infrastructure.repositories;

import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MongoWorkloadRepositoryAdapter Tests")
@ExtendWith(MockitoExtension.class)
class MongoWorkloadRepositoryAdapterTest {

    @Mock
    private MongoWorkloadRepository mongoRepository;

    @InjectMocks
    private MongoWorkloadRepositoryAdapter adapter;

    @Test
    @DisplayName("Should save workload and return saved entity")
    void testSave() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        when(mongoRepository.save(workload)).thenReturn(workload);

        TrainerWorkload saved = adapter.save(workload);

        assertNotNull(saved);
        assertEquals("john.doe", saved.getUsername());
        verify(mongoRepository).save(workload);
    }

    @Test
    @DisplayName("Should find workload by username")
    void testFindByUsername() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        when(mongoRepository.findByUsername("john.doe")).thenReturn(Optional.of(workload));

        Optional<TrainerWorkload> found = adapter.findByUsername("john.doe");

        assertTrue(found.isPresent());
        assertEquals("john.doe", found.get().getUsername());
        verify(mongoRepository).findByUsername("john.doe");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsername_NotFound() {
        when(mongoRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<TrainerWorkload> found = adapter.findByUsername("unknown");

        assertTrue(found.isEmpty());
        verify(mongoRepository).findByUsername("unknown");
    }

    @Test
    @DisplayName("Should return all workloads")
    void testFindAll() {
        TrainerWorkload w1 = TrainerWorkload.builder().username("trainer1").firstName("A").lastName("B").isActive(true).build();
        TrainerWorkload w2 = TrainerWorkload.builder().username("trainer2").firstName("C").lastName("D").isActive(true).build();

        when(mongoRepository.findAll()).thenReturn(List.of(w1, w2));

        List<TrainerWorkload> all = adapter.findAll();

        assertEquals(2, all.size());
        verify(mongoRepository).findAll();
    }
}