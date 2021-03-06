/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.microprofile.health;

import static org.eclipse.microprofile.health.HealthCheckResponse.State.UP;
import static org.wildfly.extension.microprofile.health.SubsystemExtension.SUBSYSTEM_NAME;
import static org.wildfly.extension.microprofile.health.SubsystemExtension.getResourceDescriptionResolver;

import java.util.Collection;
import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class CheckOperation implements OperationStepHandler {

   public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("check", getResourceDescriptionResolver(SUBSYSTEM_NAME))
           .setRuntimeOnly()
           .setReplyType(ModelType.OBJECT)
           .setReplyValueType(ModelType.OBJECT)
           .build();

   public static ModelNode computeResult(Collection<HealthCheckResponse> statuses) {
      ModelNode result = new ModelNode();
      boolean globalOutcome = true;
      result.get("checks").setEmptyList();
      for (HealthCheckResponse status : statuses) {
         ModelNode responseNode = new ModelNode();
         responseNode.get("name").set(status.getName());
         HealthCheckResponse.State state = status.getState();
         globalOutcome = globalOutcome & state == UP;
         responseNode.get("state").set(state.toString());
         if (status.getData().isPresent()) {
            responseNode.get("data").setEmptyObject();
            Map<String, Object> attributes = status.getData().get();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
               responseNode.get("data").get(entry.getKey()).set(entry.getValue().toString());
            }
         }
         result.get("checks").add(responseNode);
      }
      result.get("outcome").set(globalOutcome ? "UP" : "DOWN");
      return result;
   }

   @Override
   public void execute(OperationContext operationContext, ModelNode modelNode) throws OperationFailedException {
      ServiceController<?> healthMonitorService = operationContext.getServiceRegistry(false).getRequiredService(HealthMonitorService.SERVICE_NAME);
      HealthMonitor healthMonitor = HealthMonitor.class.cast(healthMonitorService.getValue());

      Collection<HealthCheckResponse> responses = healthMonitor.check();
      ModelNode result = computeResult(responses);
      operationContext.getResult().set(result);
   }
}
