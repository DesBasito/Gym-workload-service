package abu.epam.com.workloadservice.application.service;

import abu.epam.com.workloadservice.application.dto.WorkloadRequest;
import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import abu.epam.com.workloadservice.core.port.WorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final WorkloadRepository workloadRepository;

    public void processWorkload(WorkloadRequest request) {
        log.info("Processing workload for trainer: {}", request.getUsername());

        TrainerWorkload workload = workloadRepository.findByUsername(request.getUsername())
                .orElseGet(() -> TrainerWorkload.builder()
                        .username(request.getUsername())
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

        workloadRepository.save(workload);

        log.info("Workload processed successfully for trainer: {}. Total for {}-{}: {} minutes",
                request.getUsername(), year, month, workload.getTotalDuration(year, month));
    }

    public TrainerWorkload getTrainerWorkload(String username) {
        log.debug("Retrieving workload for trainer: {}", username);
        return workloadRepository.findByUsername(username).orElse(null);
    }

    public List<TrainerWorkload> getAllWorkloads() {
        log.debug("Retrieving all trainer workloads");
        return workloadRepository.findAll();
    }
}