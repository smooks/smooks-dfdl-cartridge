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

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
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
import java.util.List;

public class DfdlParser implements SmooksXMLReader {

    public static final TypedKey<List<Diagnostic>> DIAGNOSTICS_TYPED_KEY = new TypedKey<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(DfdlParser.class);

    protected DataProcessor dataProcessor;

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    private ResourceConfig resourceConfig;

    @Inject
    @Named("dataProcessorFactory")
    private Class<? extends DataProcessorFactory> dataProcessorFactoryClass;

    @Inject
    @Named("schemaUri")
    private String schemaUri;

    @Inject
    @Named("schematronValidation")
    private Boolean schematronValidation = false;

    @Inject
    private Boolean indent = false;

    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private DTDHandler dtdHandler;
    private ExecutionContext executionContext;

    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {

    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(final DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler errorHandler) {
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

    @Override
    public void parse(final InputSource input) {
        final InputSourceDataInputStream inputSourceDataInputStream = new InputSourceDataInputStream(input.getByteStream());
        ParseResult parseResult = null;
        while (parseResult == null || inputSourceDataInputStream.hasData()) {
            parseResult = dataProcessor.parse(inputSourceDataInputStream, new ContentHandlerInfosetOutputter(contentHandler, indent));
            if (parseResult.isError()) {
                executionContext.put(DIAGNOSTICS_TYPED_KEY, parseResult.getDiagnostics());
                for (Diagnostic diagnostic : parseResult.getDiagnostics()) {
                    if (diagnostic.isError() && !schematronValidation) {
                        throw new SmooksException(diagnostic.getSomeMessage(), diagnostic.getSomeCause());
                    } else {
                        LOGGER.debug(diagnostic.getSomeMessage());
                    }
                }
            }
            for (final Diagnostic diagnostic : parseResult.getDiagnostics()) {
                LOGGER.debug(diagnostic.getSomeMessage());
            }
        }
    }

    @Override
    public void parse(final String systemId) {

    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setIndent(final Boolean indent) {
        this.indent = indent;
    }

    public Class<? extends DataProcessorFactory> getDataProcessorFactoryClass() {
        return dataProcessorFactoryClass;
    }

    public void setDataProcessorFactoryClass(final Class<? extends DataProcessorFactory> dataProcessorFactoryClass) {
        this.dataProcessorFactoryClass = dataProcessorFactoryClass;
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

    public void setSchemaUri(final String schemaUri) {
        this.schemaUri = schemaUri;
    }
}
