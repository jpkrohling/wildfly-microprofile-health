package org.wildfly.extension.microprofile.health;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthResponse;
import org.eclipse.microprofile.health.HealthStatus;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.microprofile.health.deployment.DependencyProcessor;
import org.wildfly.extension.microprofile.health.deployment.SubsystemDeploymentProcessor;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class SubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    private SubsystemAdd() {
        super(SubsystemDefinition.ATTRIBUTES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {

        MicroProfileHealthLogger.ROOT_LOGGER.activatingSubsystem();

        HealthMonitorService.install(context);

        ModelNode httpEndpoint = SubsystemDefinition.HTTP_ENDPOINT.resolveModelAttribute(context, model);
        if (httpEndpoint.isDefined()) {
            HealthHttpHandlerService.install(context, httpEndpoint.asString());
        }

        HealthCheck.install(context, "heap-memory", () -> {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long memUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long memMax = memoryBean.getHeapMemoryUsage().getMax();
            HealthResponse response = HealthResponse.named("heap-memory")
                    .withAttribute("used", memUsed)
                    .withAttribute("max", memMax);
            // status is is down is used memory is greater than 90% of max memory.
            HealthStatus status = (memUsed < memMax * 0.9) ? response.up() : response.down();
            return status;
        });

        //Add deployment processors here
        //Remove this if you don't need to hook into the deployers, or you can add as many as you like
        //see SubDeploymentProcessor for explanation of the phases
        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, DependencyProcessor.PHASE, DependencyProcessor.PRIORITY, new DependencyProcessor());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, SubsystemDeploymentProcessor.PHASE, SubsystemDeploymentProcessor.PRIORITY, new SubsystemDeploymentProcessor());

            }
        }, OperationContext.Stage.RUNTIME);
    }
}
