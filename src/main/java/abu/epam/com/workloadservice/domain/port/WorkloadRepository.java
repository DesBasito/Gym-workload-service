package abu.epam.com.workloadservice.domain.port;

import abu.epam.com.workloadservice.domain.model.TrainerWorkload;

import java.util.Map;
import java.util.Optional;

public interface WorkloadRepository {

    void save(TrainerWorkload workload);

    Optional<TrainerWorkload> findByUsername(String username);

    Map<String, TrainerWorkload> findAll();
}