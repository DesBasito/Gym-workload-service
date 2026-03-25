package abu.epam.com.workloadservice.cucumber.steps;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

@Component
@ScenarioScope
public class SharedWorkloadContext {

    private MvcResult lastResult;

    public MvcResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(MvcResult lastResult) {
        this.lastResult = lastResult;
    }

    public int getLastStatus() throws Exception {
        return lastResult.getResponse().getStatus();
    }

    public String getLastResponseBody() throws Exception {
        return lastResult.getResponse().getContentAsString();
    }
}