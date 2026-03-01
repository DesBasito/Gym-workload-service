package abu.epam.com.workloadservice.infrastructure.repositories;

import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@DisplayName("MongoWorkloadRepository Integration Tests (Embedded MongoDB)")
class MongoWorkloadRepositoryTest {

    @Autowired
    private MongoWorkloadRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve trainer workload by username")
    void testSaveAndFindByUsername() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        repository.save(workload);

        Optional<TrainerWorkload> found = repository.findByUsername("john.doe");

        assertTrue(found.isPresent());
        assertEquals("john.doe", found.get().getUsername());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
        assertTrue(found.get().getIsActive());
        assertNotNull(found.get().getId());
    }

    @Test
    @DisplayName("Should return empty Optional when username not found")
    void testFindByUsername_NotFound() {
        Optional<TrainerWorkload> found = repository.findByUsername("non.existent");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should save workload with nested years and months structure")
    void testSaveWithNestedStructure() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("jane.smith")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();

        workload.updateWorkload(2026, 1, 60);
        workload.updateWorkload(2026, 2, 90);
        workload.updateWorkload(2025, 12, 45);

        repository.save(workload);

        Optional<TrainerWorkload> found = repository.findByUsername("jane.smith");

        assertTrue(found.isPresent());
        TrainerWorkload saved = found.get();
        assertEquals(60, saved.getTotalDuration(2026, 1));
        assertEquals(90, saved.getTotalDuration(2026, 2));
        assertEquals(45, saved.getTotalDuration(2025, 12));
    }

    @Test
    @DisplayName("Should update existing workload document")
    void testUpdateExistingWorkload() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("bob.trainer")
                .firstName("Bob")
                .lastName("Trainer")
                .isActive(true)
                .build();
        workload.updateWorkload(2026, 3, 60);
        repository.save(workload);

        TrainerWorkload existing = repository.findByUsername("bob.trainer").orElseThrow();
        existing.updateWorkload(2026, 3, 30);
        existing.setFirstName("Robert");
        repository.save(existing);

        TrainerWorkload updated = repository.findByUsername("bob.trainer").orElseThrow();
        assertEquals("Robert", updated.getFirstName());
        assertEquals(90, updated.getTotalDuration(2026, 3));
    }

    @Test
    @DisplayName("Should find all workloads")
    void testFindAll() {
        repository.saveAll(List.of(
                TrainerWorkload.builder().username("t1").firstName("A").lastName("B").isActive(true).build(),
                TrainerWorkload.builder().username("t2").firstName("C").lastName("D").isActive(true).build(),
                TrainerWorkload.builder().username("t3").firstName("E").lastName("F").isActive(false).build()
        ));

        List<TrainerWorkload> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("Should enforce unique username constraint")
    void testUniqueUsernameConstraint() {
        repository.save(TrainerWorkload.builder()
                .username("unique.user").firstName("A").lastName("B").isActive(true).build());

        TrainerWorkload duplicate = TrainerWorkload.builder()
                .username("unique.user").firstName("C").lastName("D").isActive(true).build();

        assertThrows(Exception.class, () -> repository.save(duplicate));
    }

    @Test
    @DisplayName("Should delete workload by id")
    void testDeleteById() {
        TrainerWorkload saved = repository.save(TrainerWorkload.builder()
                .username("to.delete").firstName("Del").lastName("Ete").isActive(true).build());

        repository.deleteById(saved.getId());

        assertTrue(repository.findByUsername("to.delete").isEmpty());
    }

    @Test
    @DisplayName("Should have compound index on firstName and lastName")
    void testCompoundIndexExists() {
        repository.save(TrainerWorkload.builder()
                .username("index.test").firstName("Test").lastName("User").isActive(true).build());

        List<IndexInfo> indexes = mongoTemplate.indexOps("trainer_workloads").getIndexInfo();

        boolean hasNameIndex = indexes.stream()
                .anyMatch(index -> "name_index".equals(index.getName()));

        assertTrue(hasNameIndex, "Compound index 'name_index' on firstName+lastName should exist");
    }

    @Test
    @DisplayName("Should have unique index on username")
    void testUsernameIndexExists() {
        repository.save(TrainerWorkload.builder()
                .username("idx.test").firstName("Idx").lastName("Test").isActive(true).build());

        List<IndexInfo> indexes = mongoTemplate.indexOps("trainer_workloads").getIndexInfo();

        boolean hasUsernameIndex = indexes.stream()
                .anyMatch(index -> index.isUnique() &&
                        index.getIndexFields().stream()
                                .anyMatch(field -> "username".equals(field.getKey())));

        assertTrue(hasUsernameIndex, "Unique index on 'username' should exist");
    }

    @Test
    @DisplayName("Should persist training summary duration as number type")
    void testDurationIsNumberType() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .username("num.test").firstName("Num").lastName("Test").isActive(true).build();
        workload.updateWorkload(2026, 5, 120);
        repository.save(workload);

        TrainerWorkload found = repository.findByUsername("num.test").orElseThrow();
        assertEquals(120, found.getTotalDuration(2026, 5));
    }

    @Test
    @DisplayName("Should persist trainer status as Boolean type")
    void testStatusIsBooleanType() {
        repository.saveAll(List.of(
                TrainerWorkload.builder().username("active.t").firstName("A").lastName("B").isActive(true).build(),
                TrainerWorkload.builder().username("inactive.t").firstName("C").lastName("D").isActive(false).build()
        ));

        assertTrue(repository.findByUsername("active.t").orElseThrow().getIsActive());
        assertFalse(repository.findByUsername("inactive.t").orElseThrow().getIsActive());
    }
}