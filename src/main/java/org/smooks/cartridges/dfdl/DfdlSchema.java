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
import org.apache.daffodil.validation.schematron.SchSource;
import org.apache.daffodil.validation.schematron.SchematronValidator;
import org.apache.daffodil.validation.schematron.SchematronValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.resource.URIResourceLocator;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.util.List;

public class DfdlSchema {

    public static final String WORKING_DIRECTORY = ".smooks/dfdl-cartridge/";
    protected static final Logger LOGGER = LoggerFactory.getLogger(DfdlSchema.class);

    protected final URI uri;
    protected final ValidationMode validationMode;
    protected final boolean cacheOnDisk;
    protected final boolean debugging;
    protected final String distinguishedRootNode;
    private final String schematronUrl;
    private final boolean schematronValidation;

    public DfdlSchema(final URI uri, final ValidationMode validationMode, final boolean cacheOnDisk,
                      final boolean debugging, final String distinguishedRootNode, final String schematronUrl,
                      final boolean schematronValidation) {
        this.uri = uri;
        this.validationMode = validationMode;
        this.cacheOnDisk = cacheOnDisk;
        this.debugging = debugging;
        this.distinguishedRootNode = distinguishedRootNode;
        this.schematronUrl = schematronUrl;
        this.schematronValidation = schematronValidation;
    }

    public URI getUri() {
        return uri;
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
        return uri + ":" + validationMode + ":" + cacheOnDisk + ":" + debugging;
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
                dataProcessor.save(Channels.newChannel(Files.newOutputStream(binSchemaFile.toPath())));
            }
        } else {
            dataProcessor = compileSource();
        }

        if (debugging) {
            dataProcessor = dataProcessor.withDebuggerRunner(new TraceDebuggerRunner()).withDebugging(true);
        }


        dataProcessor = dataProcessor.withValidationMode(validationMode);

        if (schematronValidation) {
            final SchematronValidator schematronValidator;
            URIResourceLocator uriResourceLocator = new URIResourceLocator();
            if (schematronUrl != null) {
                InputStream schematronInputStream = uriResourceLocator.getResource(schematronUrl);
                schematronValidator = SchematronValidatorFactory.makeValidator(schematronInputStream, schematronUrl, new SchSource.Sch$());
            } else {
                InputStream schematronInputStream = uriResourceLocator.getResource(uri.toString());
                schematronValidator = SchematronValidatorFactory.makeValidator(schematronInputStream, uri.toString(), new SchSource.Xsd$());
            }
            dataProcessor = dataProcessor.withValidator(schematronValidator);
        }

        return dataProcessor;
    }

    protected DataProcessor compileSource() throws Throwable {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        final ProcessorFactory processorFactory;
        if (distinguishedRootNode != null) {
            processorFactory = compiler.compileSource(uri, distinguishedRootNode.substring(distinguishedRootNode.indexOf("}") + 1), distinguishedRootNode.substring(distinguishedRootNode.indexOf("{") + 1, distinguishedRootNode.indexOf("}")));
        } else {
            processorFactory = compiler.compileSource(uri);
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
