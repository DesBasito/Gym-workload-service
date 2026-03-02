package abu.epam.com.workloadservice.infrastructure.repositories;

import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import abu.epam.com.workloadservice.core.port.WorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoWorkloadRepositoryAdapter implements WorkloadRepository {

    private final MongoWorkloadRepository mongoRepository;

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        log.debug("Saving workload for trainer: {}", workload.getUsername());
        return mongoRepository.save(workload);
    }

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        log.debug("Finding workload for trainer: {}", username);
        return mongoRepository.findByUsername(username);
    }

    @Override
    public List<TrainerWorkload> findAll() {
        log.debug("Retrieving all trainer workloads");
        return mongoRepository.findAll();
    }
}