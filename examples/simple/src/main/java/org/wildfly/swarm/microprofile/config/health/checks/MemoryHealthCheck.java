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

package org.wildfly.swarm.microprofile.config.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.health.HealthCheckProcedure;
import org.eclipse.microprofile.health.HealthResponse;
import org.eclipse.microprofile.health.HealthStatus;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
@Dependent
public class MemoryHealthCheck implements HealthCheckProcedure {

   @Override
   public HealthStatus perform() {
      System.out.println("MemoryHealthCheck.perform");
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long memUsed = memoryBean.getHeapMemoryUsage().getUsed();
      long memMax = memoryBean.getHeapMemoryUsage().getMax();
      HealthResponse response = HealthResponse.named("heap-memory")
              .withAttribute("used", memUsed)
              .withAttribute("max", memMax);
      HealthStatus status = (memUsed < memMax * 0.8) ? response.up() : response.down();
      System.out.println("status = " + status);
      return status;
   }
}
