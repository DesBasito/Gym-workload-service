package abu.epam.com.workloadservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    private String username;
    private String firstName;
    private String lastName;
    private Boolean isActive;

    @Builder.Default
    private Map<Integer, TrainerYearlySummary> years = new HashMap<>();

    public void updateWorkload(int year, int month, int duration) {
        years.computeIfAbsent(year, k -> new TrainerYearlySummary())
                .updateMonth(month, duration);
    }

    public int getTotalDuration(int year, int month) {
        return years.getOrDefault(year, new TrainerYearlySummary())
                .getMonthDuration(month);
    }
}
