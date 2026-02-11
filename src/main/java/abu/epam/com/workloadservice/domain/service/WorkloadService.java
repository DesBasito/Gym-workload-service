package abu.epam.com.workloadservice.domain.service;

import abu.epam.com.workloadservice.domain.dto.WorkloadRequest;
import abu.epam.com.workloadservice.domain.model.TrainerWorkload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WorkloadService {

    private final Map<String, TrainerWorkload> workloadStorage = new ConcurrentHashMap<>();

    public void processWorkload(WorkloadRequest request) {
        log.info("Processing workload for trainer: {}", request.getUsername());

        TrainerWorkload workload = workloadStorage.computeIfAbsent(
                request.getUsername(),
                username -> TrainerWorkload.builder()
                        .username(username)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isActive(request.getIsActive())
                        .build()
        );

        workload.setFirstName(request.getFirstName());
        workload.setLastName(request.getLastName());
        workload.setIsActive(request.getIsActive());

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int duration = request.getTrainingDuration();

        switch (request.getActionType()) {
            case ADD:
                log.debug("Adding {} minutes to {}-{} for trainer {}",
                        duration, year, month, request.getUsername());
                workload.updateWorkload(year, month, duration);
                break;
            case DELETE:
                log.debug("Removing {} minutes from {}-{} for trainer {}",
                        duration, year, month, request.getUsername());
                workload.updateWorkload(year, month, -duration);
                break;
        }

        log.info("Workload processed successfully for trainer: {}. Total for {}-{}: {} minutes",
                request.getUsername(), year, month, workload.getTotalDuration(year, month));
    }

    public TrainerWorkload getTrainerWorkload(String username) {
        log.debug("Retrieving workload for trainer: {}", username);
        return workloadStorage.get(username);
    }

    public Map<String, TrainerWorkload> getAllWorkloads() {
        log.debug("Retrieving all trainer workloads");
        return Map.copyOf(workloadStorage);
    }
}
