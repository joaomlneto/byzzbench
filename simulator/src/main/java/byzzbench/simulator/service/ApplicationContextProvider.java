package byzzbench.simulator.service;

import byzzbench.simulator.config.ByzzBenchConfig;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * Get the ScenarioService bean from the application context.
     *
     * @return the ScenarioService bean
     */
    public static ScenarioService getScenarioService() {
        return applicationContext.getBean(ScenarioService.class);
    }

    public static MessageMutatorService getMessageMutatorService() {
        return applicationContext.getBean(MessageMutatorService.class);
    }

    public static ByzzBenchConfig getConfig() {
        return applicationContext.getBean(ByzzBenchConfig.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}
