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
package org.smooks.cartridges.dfdl.unparser;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.daffodil.japi.DaffodilUnparseContentHandler;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ExternalVariableException;
import org.apache.daffodil.japi.UnparseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.StreamResultWriter;
import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.io.Stream;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.helpers.AttributesImpl;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StreamResultWriter
public class DfdlUnparser implements BeforeVisitor, AfterVisitor, ChildrenVisitor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DfdlUnparser.class);
    protected final DataProcessor dataProcessor;

    @Inject
    protected ResourceConfig resourceConfig;

    public DfdlUnparser(final DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        final DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = getOrCreateDaffodilUnparseContentHandlerMemento(element, executionContext);
        final DaffodilUnparseContentHandler daffodilUnparseContentHandler = daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler();
        if (element.getPrefix() == null || element.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            daffodilUnparseContentHandler.endElement(XMLConstants.NULL_NS_URI, element.getLocalName(), element.getLocalName());
        } else {
            daffodilUnparseContentHandler.endElement(element.getNamespaceURI(), element.getLocalName(), element.getPrefix()  + ":" + element.getLocalName());
        }

        throwIfError(daffodilUnparseContentHandler.getUnparseResult());
        
        if (daffodilUnparseContentHandlerMemento.getFragment().unwrap().equals(element)) {
            daffodilUnparseContentHandler.endDocument();
            throwIfError(daffodilUnparseContentHandler.getUnparseResult());
        }
    }

    protected DaffodilUnparseContentHandlerMemento getOrCreateDaffodilUnparseContentHandlerMemento(final Node node, final ExecutionContext executionContext) {
        Node parentNode = node;
        final MementoCaretaker mementoCaretaker = executionContext.getMementoCaretaker();
        while (parentNode != null) { 
            final DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = new DaffodilUnparseContentHandlerMemento(new NodeFragment(parentNode), this);
            final boolean exists = mementoCaretaker.exists(daffodilUnparseContentHandlerMemento);
            if (exists) {
                mementoCaretaker.restore(daffodilUnparseContentHandlerMemento);
                return daffodilUnparseContentHandlerMemento;
            } else {
                parentNode = parentNode.getParentNode();
            }
        }

        final WritableByteChannel writableByteChannel;
        final DaffodilUnparseContentHandler daffodilUnparseContentHandler;
        try {
            writableByteChannel = Channels.newChannel(WriterOutputStream.builder().setCharset(executionContext.getContentEncoding()).setBufferSize(1024).setWriteImmediately(true).setWriter(Stream.out(executionContext)).get());
            daffodilUnparseContentHandler = dataProcessor.withExternalVariables(getVariables(executionContext)).newContentHandlerInstance(writableByteChannel);
        } catch (ExternalVariableException | IOException e) {
            throw new SmooksException(e);
        }
        daffodilUnparseContentHandler.startDocument();
       
        final DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = new DaffodilUnparseContentHandlerMemento(new NodeFragment(node), this);
        daffodilUnparseContentHandlerMemento.setDaffodilUnparseContentHandler(daffodilUnparseContentHandler);
        mementoCaretaker.capture(daffodilUnparseContentHandlerMemento);
        
        return daffodilUnparseContentHandlerMemento;
    }

    protected AbstractMap<String, String> getVariables(ExecutionContext executionContext)  {
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
    public void visitBefore(Element element, ExecutionContext executionContext) {
        final DaffodilUnparseContentHandler daffodilUnparseContentHandler = getOrCreateDaffodilUnparseContentHandlerMemento(element, executionContext).getDaffodilUnparseContentHandler();
        
        final AttributesImpl attributes = new AttributesImpl();
        if (element.getAttributes() != null) {
            final NamedNodeMap namedNodeMap = element.getAttributes();
            for (int i = 0; i < namedNodeMap.getLength(); i++) {
                final Node node = namedNodeMap.item(i);
                if (node.getPrefix() == null || node.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    attributes.addAttribute(XMLConstants.NULL_NS_URI, node.getLocalName(), node.getNodeName(), String.valueOf(node.getNodeType()), node.getNodeValue());
                } else {
                    attributes.addAttribute(node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), String.valueOf(node.getNodeType()), node.getNodeValue());
                }
            }
        }

        if (element.getPrefix() == null || element.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            daffodilUnparseContentHandler.startElement(XMLConstants.NULL_NS_URI, element.getLocalName(), element.getLocalName(), attributes);
        } else {
            daffodilUnparseContentHandler.startElement(element.getNamespaceURI(), element.getLocalName(), element.getPrefix()  + ":" + element.getLocalName(), attributes);
        }

        throwIfError(daffodilUnparseContentHandler.getUnparseResult());
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) {
        final DaffodilUnparseContentHandler daffodilUnparseContentHandler = getOrCreateDaffodilUnparseContentHandlerMemento(characterData, executionContext).getDaffodilUnparseContentHandler();
        daffodilUnparseContentHandler.characters(characterData.getData().toCharArray(), 0, characterData.getData().length());
        throwIfError(daffodilUnparseContentHandler.getUnparseResult());
    }

    @Override
    public void visitChildElement(Element element, ExecutionContext executionContext) {

    }
    
    protected void throwIfError(final UnparseResult unparseResult) {
        if (unparseResult != null) {
            for (Diagnostic diagnostic : unparseResult.getDiagnostics()) {
                if (diagnostic.isError()) {
                    throw new SmooksException(diagnostic.getSomeMessage(), diagnostic.getSomeCause());
                } else {
                    LOGGER.warn(diagnostic.getMessage());
                }
            }
        }
    }

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public void setResourceConfig(ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }
}
