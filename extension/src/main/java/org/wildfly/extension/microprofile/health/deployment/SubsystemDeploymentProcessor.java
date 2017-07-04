package org.wildfly.extension.microprofile.health.deployment;

import org.jboss.as.ee.weld.WeldDeploymentMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.weld.deployment.WeldPortableExtensions;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;

/**
 */
public class SubsystemDeploymentProcessor implements DeploymentUnitProcessor {

    Logger log = Logger.getLogger(SubsystemDeploymentProcessor.class);

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.POST_MODULE;

    /**
     * The relative order of this processor within the {@link #PHASE}.
     * The current number is large enough for it to happen after all
     * the standard deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4000;

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        System.out.println("SubsystemDeploymentProcessor.deploy");
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
            System.out.println("SubsystemDeploymentProcessor.deploy WELD");
            WeldPortableExtensions extensions = WeldPortableExtensions.getPortableExtensions(deploymentUnit);
            extensions.registerExtensionInstance(new CDIExtension(), deploymentUnit);
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
