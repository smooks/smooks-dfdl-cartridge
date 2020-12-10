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
package org.smooks.cartridges.dfdl.parser;

import org.apache.daffodil.infoset.DIArray;
import org.apache.daffodil.infoset.DIComplex;
import org.apache.daffodil.infoset.DIElement;
import org.apache.daffodil.infoset.DISimple;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.injector.Scope;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.registry.lookup.LifecycleManagerLookup;
import org.smooks.xml.SmooksXMLReader;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.XMLConstants;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

public class DfdlParser implements SmooksXMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DfdlParser.class);
    private static final char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    protected DataProcessor dataProcessor;

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    private ResourceConfig resourceConfig;

    @Inject
    @Named("dataProcessorFactory")
    private Class<? extends DataProcessorFactory> dataProcessorFactoryClass;

    @Inject
    @Named("schemaURI")
    private String schemaUri;

    @Inject
    private Boolean indent = false;

    private DataProcessorFactory dataProcessorFactory;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private DTDHandler dtdHandler;

    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {

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
    public void initialize() throws IllegalAccessException, InstantiationException {
        dataProcessorFactory = dataProcessorFactoryClass.newInstance();
        applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(dataProcessorFactory, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), resourceConfig, dataProcessorFactory)));
        dataProcessor = dataProcessorFactory.createDataProcessor();
    }

    @Override
    public void parse(final InputSource input) {
        final InputSourceDataInputStream inputSourceDataInputStream = new InputSourceDataInputStream(input.getByteStream());
        ParseResult parseResult = null;
        while (parseResult == null || !parseResult.location().isAtEnd()) {
            parseResult = dataProcessor.parse(inputSourceDataInputStream, new InfosetOutputter() {
                private int elementLevel = 0;

                @Override
                public void reset() {

                }

                @Override
                public boolean startDocument() {
                    try {
                        contentHandler.startDocument();
                    } catch (SAXException e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public boolean endDocument() {
                    try {
                        contentHandler.endDocument();
                    } catch (SAXException e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public boolean startSimple(final DISimple diSimple) {
                    try {
                        final AttributesImpl attributes = createAttributes(diSimple);
                        if (isNilled(diSimple)) {
                            attributes.addAttribute(W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "xsi:nil", "NMTOKEN", "true");
                        }
                        indent(elementLevel);
                        contentHandler.startElement(getNamespaceUri(diSimple), diSimple.erd().name(), getQName(diSimple), attributes);
                        if (!isNilled(diSimple) && diSimple.hasValue()) {
                            contentHandler.characters(diSimple.dataValueAsString().toCharArray(), 0, diSimple.dataValueAsString().length());
                        }
                    } catch (Exception e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public boolean endSimple(final DISimple diSimple) {
                    try {
                        contentHandler.endElement(getNamespaceUri(diSimple), diSimple.erd().name(), getQName(diSimple));
                    } catch (Exception e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public boolean startComplex(final DIComplex diComplex) {
                    try {
                        indent(elementLevel);
                        final String nsUri = getNamespaceUri(diComplex);
                        contentHandler.startElement(nsUri, diComplex.erd().name(), getQName(diComplex), createAttributes(diComplex));
                        elementLevel++;
                        if (diComplex.isEmpty()) {
                            elementLevel--;
                            contentHandler.endElement(nsUri, diComplex.erd().name(), getQName(diComplex));
                        }
                    } catch (SAXException e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                private AttributesImpl createAttributes(final DIElement diElement) {
                    final NamespaceBinding nsbStart = diElement.erd().minimizedScope();
                    final NamespaceBinding nsbEnd = diElement.isRoot() ? TopScope$.MODULE$ : diElement.diParent().erd().minimizedScope();
                    final AttributesImpl attributes = new AttributesImpl();
                    if (nsbStart != nsbEnd) {
                        NamespaceBinding namespaceBinding = nsbStart;
                        while (namespaceBinding != TopScope$.MODULE$) {
                            attributes.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceBinding.prefix(), XMLConstants.XMLNS_ATTRIBUTE + ":" + namespaceBinding.prefix(), "CDATA", namespaceBinding.uri());
                            namespaceBinding = namespaceBinding.copy$default$3();
                        }
                    }

                    return attributes;
                }

                @Override
                public boolean endComplex(final DIComplex diComplex) {
                    try {
                        elementLevel--;
                        indent(elementLevel);
                        contentHandler.endElement(diComplex.erd().targetNamespace().toString(), diComplex.erd().name(), getQName(diComplex));
                    } catch (SAXException e) {
                        throw new SmooksException(e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public boolean startArray(final DIArray diArray) {
                    return true;
                }

                @Override
                public boolean endArray(final DIArray diArray) {
                    return true;
                }

                private String getQName(final DIElement diElement) {
                    final String prefix = diElement.erd().namedQName().prefixOrNull();
                    return (prefix == null || prefix.equals("")) ? "" : prefix + ":" + diElement.erd().name();
                }
            });
            if (parseResult.isError()) {
                for (Diagnostic diagnostic : parseResult.getDiagnostics()) {
                    if (diagnostic.isError()) {
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

    private void indent(final int elementLevel) throws SAXException {
        if (indent) {
            contentHandler.characters(INDENT, 0, elementLevel + 1);
        }
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
    
    private String getNamespaceUri(final DIElement diElement) {
        return diElement.erd().namedQName().namespace().isNoNamespace() ? NULL_NS_URI : diElement.erd().namedQName().namespace().toString();
    }
}
