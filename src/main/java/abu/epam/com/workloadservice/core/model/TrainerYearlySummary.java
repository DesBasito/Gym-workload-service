package abu.epam.com.workloadservice.core.model;

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
public class TrainerYearlySummary {

    @Builder.Default
    private Map<String, TrainerMonthlySummary> months = new HashMap<>();

    public void updateMonth(int month, int duration) {
        String monthKey = String.valueOf(month);
        months.computeIfAbsent(monthKey, k -> new TrainerMonthlySummary())
                .addDuration(duration);
    }

    public int getMonthDuration(int month) {
        String monthKey = String.valueOf(month);
        return months.getOrDefault(monthKey, new TrainerMonthlySummary())
                .getTotalDuration();
    }
}