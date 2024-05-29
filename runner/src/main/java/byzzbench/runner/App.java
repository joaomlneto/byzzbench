package byzzbench.runner;

import byzzbench.runner.service.SimulatorService;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import jakarta.inject.Inject;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "ByzzBench API",
                version = "0.1",
                description = "Byzantine Fault Tolerance Testing Benchmark API",
                license = @License(name = "MIT", url = "https://github.com/joaomlneto/byzzbench/blob/main/LICENSE"),
                contact = @Contact(name = "Joao Neto", email = "J.M.LouroNeto@tudelft.nl")
        )

)
public class App {
    @Inject
    SimulatorService sim;

    public static void main(String[] args) {
        Micronaut.run(App.class, args);
    }
}
