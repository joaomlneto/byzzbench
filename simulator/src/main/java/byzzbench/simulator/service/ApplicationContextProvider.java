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

    /**
     * Get the MessageMutatorService bean from the application context.
     *
     * @return the MessageMutatorService bean
     */
    public static MessageMutatorService getMessageMutatorService() {
        return applicationContext.getBean(MessageMutatorService.class);
    }

    /**
     * Get the ByzzBenchConfig bean from the application context.
     *
     * @return the ByzzBenchConfig bean
     */
    public static ByzzBenchConfig getConfig() {
        return applicationContext.getBean(ByzzBenchConfig.class);
    }

    /**
     * Get the FaultsFactoryService bean from the application context.
     *
     * @return the FaultsFactoryService bean
     */
    public static FaultsFactoryService getFaultsFactoryService() {
        return applicationContext.getBean(FaultsFactoryService.class);
    }

    /**
     * Get the CampaignService bean from the application context.
     *
     * @return the CampaignService bean
     */
    public static CampaignService getCampaignService() {
        return applicationContext.getBean(CampaignService.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}
