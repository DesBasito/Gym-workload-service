package abu.epam.com.workloadservice.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMonthlySummary {

    @Builder.Default
    private int totalDuration = 0;

    public void addDuration(int duration) {
        this.totalDuration = Math.max(0, this.totalDuration + duration);
    }

    public void removeDuration(int duration) {
        this.totalDuration = Math.max(0, this.totalDuration - duration);
    }
}