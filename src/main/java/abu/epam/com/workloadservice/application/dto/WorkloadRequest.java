package abu.epam.com.workloadservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating trainer workload")
public class WorkloadRequest {

    @Schema(description = "Trainer's unique username", example = "john.doe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Trainer's first name", example = "John")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "Trainer's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "Trainer's active status", example = "true")
    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @Schema(description = "Training date", example = "2024-02-08")
    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @Schema(description = "Training duration in minutes", example = "60")
    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;

    @Schema(description = "Action type: ADD (add training) or DELETE (remove training)", example = "ADD")
    @NotNull(message = "Action type is required")
    private ActionType actionType;

    @Schema(description = "Action type enumeration")
    public enum ActionType {
        @Schema(description = "Add training session")
        ADD,
        @Schema(description = "Delete training session")
        DELETE
    }
}