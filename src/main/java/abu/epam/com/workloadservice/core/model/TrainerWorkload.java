package abu.epam.com.workloadservice.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "name_index", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String firstName;
    private String lastName;
    private Boolean isActive;

    @Builder.Default
    private Map<String, TrainerYearlySummary> years = new HashMap<>();

    public void updateWorkload(int year, int month, int duration) {
        String yearKey = String.valueOf(year);
        years.computeIfAbsent(yearKey, k -> new TrainerYearlySummary())
                .updateMonth(month, duration);
    }

    public int getTotalDuration(int year, int month) {
        String yearKey = String.valueOf(year);
        return years.getOrDefault(yearKey, new TrainerYearlySummary())
                .getMonthDuration(month);
    }
}