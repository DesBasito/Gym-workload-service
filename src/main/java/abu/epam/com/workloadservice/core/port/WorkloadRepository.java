package abu.epam.com.workloadservice.core.port;

import abu.epam.com.workloadservice.core.model.TrainerWorkload;

import java.util.List;
import java.util.Optional;

public interface WorkloadRepository {

    TrainerWorkload save(TrainerWorkload workload);

    Optional<TrainerWorkload> findByUsername(String username);

    List<TrainerWorkload> findAll();
}