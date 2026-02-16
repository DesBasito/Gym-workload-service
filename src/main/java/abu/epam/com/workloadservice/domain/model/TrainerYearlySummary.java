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
public class TrainerYearlySummary {

    @Builder.Default
    private Map<Integer, TrainerMonthlySummary> months = new HashMap<>();

    public void updateMonth(int month, int duration) {
        months.computeIfAbsent(month, k -> new TrainerMonthlySummary())
                .addDuration(duration);
    }

    public int getMonthDuration(int month) {
        return months.getOrDefault(month, new TrainerMonthlySummary())
                .getTotalDuration();
    }
}
