/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.jboss.as.server.deployment.module;

import org.jboss.as.server.logging.ServerLogger;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
import org.jboss.modules.ClassTransformer;

/**
 * A {@link DeploymentUnitProcessor} that instantiates {@link ClassTransformer}s defined in the
 * <code>jboss-deployment-structure.xml</code> file.
 *
 * @author Marius Bogoevici
 */
public class ClassTransformerProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DelegatingClassTransformer transformer = deploymentUnit.getAttachment(DelegatingClassTransformer.ATTACHMENT_KEY);
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        if (module == null || transformer == null) {
            return;
        }
        try {
            for (String transformerClassName : moduleSpecification.getClassTransformers()) {
                transformer.addTransformer((ClassTransformer) module.getClassLoader().loadClass(transformerClassName).newInstance());
            }
            // activate transformer only after all delegate transformers have been added
            // so that transformers themselves are not instrumented
            transformer.setActive(true);
        } catch (Exception e) {
            throw ServerLogger.ROOT_LOGGER.failedToInstantiateClassTransformer(ClassTransformer.class.getSimpleName(), e);
        }
    }

}
