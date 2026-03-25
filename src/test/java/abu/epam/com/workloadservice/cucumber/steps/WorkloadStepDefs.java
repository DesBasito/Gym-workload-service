package abu.epam.com.workloadservice.cucumber.steps;

import abu.epam.com.workloadservice.application.dto.WorkloadRequest;
import abu.epam.com.workloadservice.core.model.TrainerWorkload;
import abu.epam.com.workloadservice.cucumber.util.JwtTestUtil;
import abu.epam.com.workloadservice.infrastructure.repositories.MongoWorkloadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class WorkloadStepDefs {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SharedWorkloadContext context;

    @Autowired
    private MongoWorkloadRepository workloadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void cleanDatabase() {
        workloadRepository.deleteAll();
    }

    private String authToken() {
        return "Bearer " + JwtTestUtil.generateToken("test-user");
    }

    @Given("no workload exists for trainer {string}")
    public void noWorkloadExistsForTrainer(String username) {
        workloadRepository.findByUsername(username).ifPresent(workloadRepository::delete);
    }

    @Given("a workload exists for trainer {string} with {int} minutes in year {int} month {int}")
    public void aWorkloadExistsForTrainer(String username, int minutes, int year, int month) {
        TrainerWorkload workload = workloadRepository.findByUsername(username)
                .orElse(TrainerWorkload.builder()
                        .username(username)
                        .firstName(username.split("\\.")[0])
                        .lastName(username.split("\\.")[1])
                        .isActive(true)
                        .build());
        workload.updateWorkload(year, month, minutes);
        workloadRepository.save(workload);
    }

    @Given("a workload exists for trainer {string}")
    public void aWorkloadExistsForTrainer(String username) {
        aWorkloadExistsForTrainer(username, 60, 2025, 6);
    }

    @Given("workloads exist for trainers {string} and {string}")
    public void workloadsExistForTrainers(String username1, String username2) {
        aWorkloadExistsForTrainer(username1, 60, 2025, 6);
        aWorkloadExistsForTrainer(username2, 90, 2025, 7);
    }

    @When("I POST a workload request for trainer {string} firstName {string} lastName {string} action {string} duration {int} date {string}")
    public void iPostWorkloadRequest(String username, String firstName, String lastName,
                                     String action, int duration, String date) throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .trainingDate(LocalDate.parse(date))
                .trainingDuration(duration)
                .actionType(WorkloadRequest.ActionType.valueOf(action))
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/workload")
                        .header("Authorization", authToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        context.setLastResult(result);
    }

    @When("I POST a workload request with missing username")
    public void iPostWorkloadRequestWithMissingUsername() throws Exception {
        String body = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "isActive": true,
                    "trainingDate": "2025-06-01",
                    "trainingDuration": 60,
                    "actionType": "ADD"
                }
                """;
        MvcResult result = mockMvc.perform(
                post("/api/workload")
                        .header("Authorization", authToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andReturn();
        context.setLastResult(result);
    }

    @When("I POST a workload request with missing firstName")
    public void iPostWorkloadRequestWithMissingFirstName() throws Exception {
        String body = """
                {
                    "username": "John.Doe",
                    "lastName": "Doe",
                    "isActive": true,
                    "trainingDate": "2025-06-01",
                    "trainingDuration": 60,
                    "actionType": "ADD"
                }
                """;
        MvcResult result = mockMvc.perform(
                post("/api/workload")
                        .header("Authorization", authToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andReturn();
        context.setLastResult(result);
    }

    @When("I POST an unauthenticated workload request for trainer {string}")
    public void iPostUnauthenticatedWorkloadRequest(String username) throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 6, 1))
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();

        context.setLastResult(result);
    }

    @When("I GET workload for trainer {string}")
    public void iGetWorkloadForTrainer(String username) throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/workload/" + username)
                        .header("Authorization", authToken())
        ).andReturn();
        context.setLastResult(result);
    }

    @When("I GET workload unauthenticated for trainer {string}")
    public void iGetWorkloadUnauthenticatedForTrainer(String username) throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/workload/" + username)
        ).andReturn();
        context.setLastResult(result);
    }

    @When("I GET all workloads")
    public void iGetAllWorkloads() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/workload")
                        .header("Authorization", authToken())
        ).andReturn();
        context.setLastResult(result);
    }

    @When("I GET all workloads unauthenticated")
    public void iGetAllWorkloadsUnauthenticated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/workload")).andReturn();
        context.setLastResult(result);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) throws Exception {
        assertThat(context.getLastStatus()).isEqualTo(expectedStatus);
    }

    @Then("the workload for trainer {string} in year {int} month {int} is {int} minutes")
    public void theWorkloadForTrainerInYearMonthIsMinutes(String username, int year, int month, int expectedMinutes) {
        TrainerWorkload workload = workloadRepository.findByUsername(username).orElseThrow(
                () -> new AssertionError("Workload not found for trainer: " + username)
        );
        assertThat(workload.getTotalDuration(year, month)).isEqualTo(expectedMinutes);
    }

    @Then("the response contains trainer username {string}")
    public void theResponseContainsTrainerUsername(String username) throws Exception {
        String body = context.getLastResponseBody();
        assertThat(body).contains(username);
    }

    @Then("the response is a JSON array")
    public void theResponseIsAJsonArray() throws Exception {
        assertThat(context.getLastResponseBody().trim()).startsWith("[");
    }

    @Then("the response is a JSON array with at least {int} entries")
    public void theResponseIsAJsonArrayWithAtLeastEntries(int minCount) throws Exception {
        String body = context.getLastResponseBody();
        assertThat(body.trim()).startsWith("[");
        long count = body.chars().filter(c -> c == '{').count();
        assertThat(count).isGreaterThanOrEqualTo(minCount);
    }
}