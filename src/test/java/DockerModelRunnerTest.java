import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.DockerModelRunnerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

@Testcontainers
@SpringBootTest
@Disabled("Requires Docker Model Runner enabled. See https://docs.docker.com/desktop/features/model-runner/. " +
        "Run cmd docker desktop enable model-runner --tcp=12434 before running tests in this class")
public class DockerModelRunnerTest {

    private static final String DEFAULT_MODEL = OllamaModel.MISTRAL.id();
    @Container
    static DockerModelRunnerContainer DMR = new DockerModelRunnerContainer("alpine/socat");

    @Autowired
    OllamaChatModel ollamaChatModel;

    @BeforeAll
    public static void beforeAll() throws IOException, InterruptedException {
        System.out.println("Start pulling the '" + DEFAULT_MODEL + "' generative ... would take several minutes ...");

        String baseUrl = "http://%s:%d".formatted(DMR.getHost(), DMR.getMappedPort(80));

        RestAssured.given().baseUri(baseUrl).body("""
                {
                    "from": "%s"
                }
                """.formatted(DEFAULT_MODEL)).post("/models/create").prettyPeek().then().statusCode(200);

        System.out.println(DEFAULT_MODEL + " pulling competed!");
    }

    @Test
    public void test() {
        System.out.println("Testing chat");
        String response = ollamaChatModel.call("tell me a joke");
        System.out.println(response);
    }

    @SpringBootConfiguration
    static class TestConfiguration {

        @Bean
        public OllamaApi ollamaApi() {
            return OllamaApi.builder().baseUrl(DMR.getOpenAIEndpoint())
                    .build();
        }

        @Bean
        public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
            return OllamaChatModel.builder()
                    .ollamaApi(ollamaApi)
                    .defaultOptions(OllamaChatOptions.builder()
                            .model(OllamaModel.MISTRAL)
                            .build())
                    .build();
        }
    }
}
