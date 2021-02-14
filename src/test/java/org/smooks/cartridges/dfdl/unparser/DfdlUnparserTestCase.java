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

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.junit.jupiter.api.Test;
import org.smooks.cartridges.dfdl.AbstractTestCase;
import org.smooks.container.MockExecutionContext;
import org.smooks.io.Stream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DfdlUnparserTestCase extends AbstractTestCase {

    private Element fileElement;
    private Node recordNode;
    private Node fooItemNode;
    private Node barItemNode;
    private DfdlUnparser dfdlUnparser;
    private MockExecutionContext executionContext;

    @Override
    public void doBeforeEach() throws DocumentException, URISyntaxException, IOException {
        executionContext = new MockExecutionContext();
        executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, new StringWriter());
        
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");

        dfdlUnparser = new DfdlUnparser(dataProcessor.withExternalVariables(new HashMap<String, String>() {{
            this.put("{http://example.com}Delimiter", ",");
        }}));
        
        org.dom4j.Element document = DocumentHelper.createDocument().
                addElement("ex:file", "http://example.com").
                addElement("record");
        document.addElement("item").addText("foo");
        document.addElement("item").addText("bar");

        fileElement = new DOMWriter().write(document.getDocument()).getDocumentElement();
        recordNode = fileElement.getFirstChild();
        fooItemNode = recordNode.getFirstChild();
        barItemNode = recordNode.getLastChild();
    }
    
    @Test
    public void testVisitBefore() throws Exception {
        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitBefore(fileElement, executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitBefore((Element) recordNode, executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitBefore((Element) fooItemNode, executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());
        
        DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = dfdlUnparser.getOrCreateDaffodilUnparseContentHandlerMemento(fileElement, executionContext);
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().characters(fooItemNode.getTextContent().toCharArray(), 0, 3);
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, fooItemNode.getLocalName(), fooItemNode.getLocalName());

        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitBefore((Element) barItemNode, executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().characters(barItemNode.getTextContent().toCharArray(), 0, 3);
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, barItemNode.getLocalName(), fooItemNode.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, recordNode.getLocalName(), recordNode.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(fileElement.getNamespaceURI(), fileElement.getLocalName(), fileElement.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endDocument();

        assertEquals("foo,bar\n<EOF>", Stream.out(executionContext).toString());
    }

    @Test
    public void testVisitChildText() throws Exception {
        DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = dfdlUnparser.getOrCreateDaffodilUnparseContentHandlerMemento(fileElement, executionContext);
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(fileElement.getNamespaceURI(), fileElement.getLocalName(), fileElement.getPrefix()  + ":" + fileElement.getLocalName(), new AttributesImpl());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, recordNode.getLocalName(), recordNode.getLocalName(), new AttributesImpl());

        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, fooItemNode.getLocalName(), fooItemNode.getLocalName(), new AttributesImpl());
        
        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitChildText((org.w3c.dom.CharacterData) fooItemNode.getFirstChild(), executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, fooItemNode.getLocalName(), fooItemNode.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, barItemNode.getLocalName(), barItemNode.getLocalName(), new AttributesImpl());

        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitChildText((org.w3c.dom.CharacterData) barItemNode.getFirstChild(), executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, barItemNode.getLocalName(), barItemNode.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(XMLConstants.NULL_NS_URI, recordNode.getLocalName(), recordNode.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endElement(fileElement.getNamespaceURI(), fileElement.getLocalName(), fileElement.getLocalName());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().endDocument();
        
        assertEquals("foo,bar\n<EOF>", Stream.out(executionContext).toString());
    }
    
    @Test
    public void testVisitAfter() throws Exception {
        DaffodilUnparseContentHandlerMemento daffodilUnparseContentHandlerMemento = dfdlUnparser.getOrCreateDaffodilUnparseContentHandlerMemento(fileElement, executionContext);
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(fileElement.getNamespaceURI(), fileElement.getLocalName(), fileElement.getPrefix()  + ":" + fileElement.getLocalName(), new AttributesImpl());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, recordNode.getLocalName(), recordNode.getLocalName(), new AttributesImpl());
        
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, fooItemNode.getLocalName(), fooItemNode.getLocalName(), new AttributesImpl());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().characters(fooItemNode.getTextContent().toCharArray(), 0, 3);
        
        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitAfter((Element) fooItemNode, executionContext);
        assertTrue(Stream.out(executionContext).toString().isEmpty());

        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().startElement(XMLConstants.NULL_NS_URI, barItemNode.getLocalName(), barItemNode.getLocalName(), new AttributesImpl());
        daffodilUnparseContentHandlerMemento.getDaffodilUnparseContentHandler().characters(barItemNode.getTextContent().toCharArray(), 0, 3);

        assertTrue(Stream.out(executionContext).toString().isEmpty());
        dfdlUnparser.visitAfter((Element) barItemNode, executionContext);
        assertEquals("foo", Stream.out(executionContext).toString());

        assertEquals("foo", Stream.out(executionContext).toString());
        dfdlUnparser.visitAfter((Element) recordNode, executionContext);
        assertEquals("foo", Stream.out(executionContext).toString());

        assertEquals("foo", Stream.out(executionContext).toString());
        dfdlUnparser.visitAfter(fileElement, executionContext);
        assertEquals("foo,bar\n<EOF>", Stream.out(executionContext).toString());
    }
}
