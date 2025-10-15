package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class XRPLScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "xrpl";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        XRPLScenario scenarioExecutor = new XRPLScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
