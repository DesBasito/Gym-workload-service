package abu.epam.com.workloadservice.infrastructure.controller;

import abu.epam.com.workloadservice.application.dto.WorkloadRequest;
import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import abu.epam.com.workloadservice.application.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
@Tag(name = "Workload Management", description = "APIs for managing trainer workload")
@SecurityRequirement(name = "Bearer Authentication")
public class WorkloadController {

    private final WorkloadService workloadService;

    @Operation(summary = "Update trainer workload",
            description = "Add or delete training session for a trainer. This endpoint is called by Gym CRM System when trainings are created or deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workload updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> updateWorkload(
            @Parameter(description = "Workload request containing trainer info and training details", required = true)
            @Valid @RequestBody WorkloadRequest request) {
        log.info("Received workload request for trainer: {}", request.getUsername());
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainer workload",
            description = "Retrieve workload data for a specific trainer by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Trainer workload retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainerWorkload.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkload> getTrainerWorkload(
            @Parameter(description = "Trainer's username", required = true)
            @PathVariable String username) {
        log.info("Received request to get workload for trainer: {}", username);
        TrainerWorkload workload = workloadService.getTrainerWorkload(username);

        if (workload == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(workload);
    }

    @Operation(summary = "Get all trainers workload",
            description = "Retrieve workload data for all trainers in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All trainers workload retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TrainerWorkload>> getAllWorkloads() {
        log.info("Received request to get all trainer workloads");
        return ResponseEntity.ok(workloadService.getAllWorkloads());
    }
}