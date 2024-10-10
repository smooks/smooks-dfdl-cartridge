/*-
 * ========================LICENSE_START=================================
 * Smooks DFDL Cartridge
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
package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ValidationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ApplicationContext;
import org.smooks.api.resource.config.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataProcessorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessorFactory.class);

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    protected ResourceConfig resourceConfig;

    @Inject
    @Named("schemaUri")
    protected String schemaUri;

    public DataProcessor createDataProcessor() {
        final DfdlSchema dfdlSchema;
        try {
            dfdlSchema = new DfdlSchema(new URI(schemaUri),
                    ValidationMode.valueOf(resourceConfig.getParameterValue("validationMode", String.class, "Off")),
                    Boolean.parseBoolean(resourceConfig.getParameterValue("cacheOnDisk", String.class, "false")),
                    Boolean.parseBoolean(resourceConfig.getParameterValue("debugging", String.class, "false")),
                    resourceConfig.getParameterValue("distinguishedRootNode", String.class),
                    resourceConfig.getParameterValue("schematronUrl", String.class),
                    Boolean.parseBoolean(resourceConfig.getParameterValue("schematronValidation", String.class)));
        } catch (Throwable t) {
            throw new DfdlSmooksException(t);
        }
        return compileOrGet(dfdlSchema);

    }

    protected DataProcessor compileOrGet(final DfdlSchema dfdlSchema) {
        final ApplicationContext applicationContext = getApplicationContext();

        if (applicationContext.getRegistry().lookup(DataProcessorFactory.class) == null) {
            synchronized (DataProcessorFactory.class) {
                if (applicationContext.getRegistry().lookup(DataProcessorFactory.class) == null) {
                    applicationContext.getRegistry().registerObject(DataProcessorFactory.class, new ConcurrentHashMap<>());
                }
            }
        }
        final Map<String, DataProcessor> dataProcessors = applicationContext.getRegistry().lookup(DataProcessorFactory.class);

        return dataProcessors.compute(dfdlSchema.getName(), (key, dataProcessor) -> {
            if (dataProcessor == null) {
                LOGGER.info("Cache miss for key {}. Compiling and caching DFDL schema {}...", key,  dfdlSchema.getUri());
                try {
                    return dfdlSchema.compile();
                } catch (Throwable t) {
                    throw new DfdlSmooksException(t);
                }
            } else {
                LOGGER.debug("Cache hit for key {}", key);
                return dataProcessor;
            }
        });
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public void setResourceConfig(final ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public String getSchemaUri() {
        return schemaUri;
    }

    public void setSchemaUri(String schemaUri) {
        this.schemaUri = schemaUri;
    }
}
