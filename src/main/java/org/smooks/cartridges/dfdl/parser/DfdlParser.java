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
package org.smooks.cartridges.dfdl.parser;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ExternalVariableException;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.ValidationMode;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.apache.daffodil.runtime1.processors.parsers.ParseError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DfdlParser implements SmooksXMLReader {

    public static final TypedKey<List<Diagnostic>> DIAGNOSTICS_TYPED_KEY = TypedKey.of();

    private static final Logger LOGGER = LoggerFactory.getLogger(DfdlParser.class);

    protected DataProcessor dataProcessor;

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    protected ResourceConfig resourceConfig;

    @Inject
    @Named("dataProcessorFactory")
    protected Class<? extends DataProcessorFactory> dataProcessorFactoryClass;

    @Inject
    @Named("schemaUri")
    protected String schemaUri;

    @Inject
    @Named("validationMode")
    protected ValidationMode validationMode = ValidationMode.Off;

    @Inject
    protected Boolean indent = false;

    protected ContentHandler contentHandler;
    protected ErrorHandler errorHandler;
    protected DTDHandler dtdHandler;
    protected ExecutionContext executionContext;

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {

    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @PostConstruct
    public void postConstruct() throws IllegalAccessException, InstantiationException {
        DataProcessorFactory dataProcessorFactory = dataProcessorFactoryClass.newInstance();
        applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(dataProcessorFactory, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), resourceConfig, dataProcessorFactory)));
        dataProcessor = dataProcessorFactory.createDataProcessor();
    }

    protected AbstractMap<String, String> getVariables() {
        final List<Parameter<?>> variablesParameters = resourceConfig.getParameters("variables");
        final AbstractMap<String, String> variables = new HashMap<>();
        if (variablesParameters != null) {
            for (Parameter<?> variablesParameter : variablesParameters) {
                final Map.Entry<String, String> variable = (Map.Entry<String, String>) variablesParameter.getValue();
                variables.put(variable.getKey(), variable.getValue());
            }
        }

        return variables;
    }

    @Override
    public void parse(InputSource input) {
        InputStream inputStream = input.getByteStream();
        if (inputStream == null) {
            try {
                inputStream = ReaderInputStream.builder().setReader(input.getCharacterStream()).get();
            } catch (IOException e) {
                throw new ParserDfdlSmooksException(e);
            }
        }

        final InputSourceDataInputStream inputSourceDataInputStream = new InputSourceDataInputStream(inputStream);
        final DataProcessor copyDataProcessor;
        try {
            copyDataProcessor = dataProcessor.withExternalVariables(getVariables());
        } catch (ExternalVariableException e) {
            throw new ParserDfdlSmooksException(e);
        }
        ParseResult parseResult = null;
        while (parseResult == null || inputSourceDataInputStream.hasData()) {
            parseResult = copyDataProcessor.parse(inputSourceDataInputStream, new ContentHandlerInfosetOutputter(contentHandler, indent));
            if (parseResult.isError()) {
                executionContext.put(DIAGNOSTICS_TYPED_KEY, parseResult.getDiagnostics());
                for (Diagnostic diagnostic : parseResult.getDiagnostics()) {
                    if (diagnostic.isError()) {
                        if (validationMode.equals(ValidationMode.Full) || (diagnostic.getSomeCause() != null && diagnostic.getSomeCause() instanceof ParseError)) {
                            throw new ParserDfdlSmooksException(diagnostic.getSomeMessage(), diagnostic.getSomeCause());
                        } else {
                            LOGGER.error(diagnostic.getSomeMessage());
                        }
                    } else {
                        LOGGER.debug(diagnostic.getSomeMessage());
                    }
                }
            } else {
                for (Diagnostic diagnostic : parseResult.getDiagnostics()) {
                    LOGGER.debug(diagnostic.getSomeMessage());
                }
            }
        }
    }

    @Override
    public void parse(String systemId) {

    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setIndent(Boolean indent) {
        this.indent = indent;
    }

    public Class<? extends DataProcessorFactory> getDataProcessorFactoryClass() {
        return dataProcessorFactoryClass;
    }

    public void setDataProcessorFactoryClass(Class<? extends DataProcessorFactory> dataProcessorFactoryClass) {
        this.dataProcessorFactoryClass = dataProcessorFactoryClass;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public void setResourceConfig(ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public String getSchemaUri() {
        return schemaUri;
    }

    public void setSchemaUri(String schemaUri) {
        this.schemaUri = schemaUri;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }
}
