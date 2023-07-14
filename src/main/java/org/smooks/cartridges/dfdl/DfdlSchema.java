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

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ProcessorFactory;
import org.apache.daffodil.japi.ValidationMode;
import org.apache.daffodil.japi.debugger.TraceDebuggerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DfdlSchema {

    public static final String WORKING_DIRECTORY = ".smooks/dfdl-cartridge/";
    protected static final Logger LOGGER = LoggerFactory.getLogger(DfdlSchema.class);

    protected final URI uri;
    protected final Map<String, String> variables;
    protected final ValidationMode validationMode;
    protected final boolean cacheOnDisk;
    protected final boolean debugging;
    protected final String distinguishedRootNode;

    public DfdlSchema(final URI uri, final Map<String, String> variables, final ValidationMode validationMode, final boolean cacheOnDisk, final boolean debugging, final String distinguishedRootNode) {
        this.uri = uri;
        this.variables = variables;
        this.validationMode = validationMode;
        this.cacheOnDisk = cacheOnDisk;
        this.debugging = debugging;
        this.distinguishedRootNode = distinguishedRootNode;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public boolean isCacheOnDisk() {
        return cacheOnDisk;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public String getName() {
        return uri + ":" + validationMode + ":" + cacheOnDisk + ":" + debugging + ":" + variables.toString();
    }

    public DataProcessor compile() throws Throwable {
        DataProcessor dataProcessor;
        if (cacheOnDisk) {
            final File binSchemaFile = new File(WORKING_DIRECTORY + new File(uri.getPath()).getName() + ".dat");
            binSchemaFile.getParentFile().mkdirs();
            if (binSchemaFile.exists()) {
                LOGGER.info("Loading compiled DFDL schema from {}", binSchemaFile.getAbsolutePath());
                dataProcessor = Daffodil.compiler().reload(binSchemaFile);
            } else {
                dataProcessor = compileSource();
                LOGGER.info("Saving compiled DFDL schema to {}", binSchemaFile.getAbsolutePath());
                dataProcessor.save(Channels.newChannel(new FileOutputStream(binSchemaFile)));
            }
        } else {
            dataProcessor = compileSource();
        }

        if (debugging) {
            dataProcessor = dataProcessor.withDebuggerRunner(new TraceDebuggerRunner()).withDebugging(true);
        }

        return dataProcessor.withValidationMode(validationMode).withExternalVariables(new HashMap<>(variables));
    }

    protected DataProcessor compileSource() throws Throwable {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        final ProcessorFactory processorFactory = compiler.compileSource(uri);
        if (distinguishedRootNode != null) {
            processorFactory.withDistinguishedRootNode(distinguishedRootNode.substring(distinguishedRootNode.indexOf("}") + 1), distinguishedRootNode.substring(distinguishedRootNode.indexOf("{") + 1, distinguishedRootNode.indexOf("}")));
        }
        if (processorFactory.isError()) {
            final List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            throw diagnostics.get(0).getSomeCause();
        }
        final DataProcessor dataProcessor = processorFactory.onPath("/");
        if (dataProcessor.isError()) {
            final List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diagnostic : diagnostics) {
                throw diagnostic.getSomeCause();
            }
        }

        return dataProcessor;
    }
}
