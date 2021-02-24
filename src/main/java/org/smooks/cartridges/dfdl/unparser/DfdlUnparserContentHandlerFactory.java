/*-
 * ========================LICENSE_START=================================
 * smooks-dfdl-cartridge
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.dfdl.unparser;

import org.smooks.api.ApplicationContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.delivery.ContentHandlerFactory;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;

import javax.inject.Inject;

public class DfdlUnparserContentHandlerFactory implements ContentHandlerFactory<DfdlUnparser> {
    
    @Inject
    protected ApplicationContext applicationContext;

    @Override
    public DfdlUnparser create(final ResourceConfig smooksResourceConfiguration) throws SmooksConfigException {
        try {
            final String dataProcessorFactoryClassName = smooksResourceConfiguration.getParameterValue("dataProcessorFactory", String.class);
            final Class<? extends DataProcessorFactory> dataProcessorFactoryClass = (Class<? extends DataProcessorFactory>) Class.forName(dataProcessorFactoryClassName);
            final DataProcessorFactory dataProcessorFactory = dataProcessorFactoryClass.newInstance();

            applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(dataProcessorFactory, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), smooksResourceConfiguration, dataProcessorFactory)));
            final DfdlUnparser dfdlUnparser = new DfdlUnparser(dataProcessorFactory.createDataProcessor());
            applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(dfdlUnparser, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), smooksResourceConfiguration, dfdlUnparser)));

            return dfdlUnparser;
        } catch (Throwable t) {
            throw new SmooksConfigException(t);
        }
    }

    @Override
    public String getType() {
        return "dfdl";
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
