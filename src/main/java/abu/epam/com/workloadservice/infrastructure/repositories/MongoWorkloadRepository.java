package abu.epam.com.workloadservice.infrastructure.repositories;

import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByUsername(String username);
}