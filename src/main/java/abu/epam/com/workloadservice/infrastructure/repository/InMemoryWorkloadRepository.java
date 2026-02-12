package abu.epam.com.workloadservice.infrastructure.repository;

import abu.epam.com.workloadservice.domain.model.TrainerWorkload;
import abu.epam.com.workloadservice.domain.port.WorkloadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class InMemoryWorkloadRepository implements WorkloadRepository {

    private final Map<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    @Override
    public void save(TrainerWorkload workload) {
        log.debug("Saving workload for trainer: {}", workload.getUsername());
        storage.put(workload.getUsername(), workload);
    }

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        log.debug("Finding workload for trainer: {}", username);
        return Optional.ofNullable(storage.get(username));
    }

    @Override
    public Map<String, TrainerWorkload> findAll() {
        log.debug("Retrieving all trainer workloads. Count: {}", storage.size());
        return Map.copyOf(storage);
    }
}