/*-
 * ========================LICENSE_START=================================
 * Smooks DFDL Cartridge
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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

import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.apache.daffodil.runtime1.api.InfosetArray;
import org.apache.daffodil.runtime1.api.InfosetComplexElement;
import org.apache.daffodil.runtime1.api.InfosetElement;
import org.apache.daffodil.runtime1.api.InfosetSimpleElement;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import scala.xml.NamespaceBinding;

import javax.xml.XMLConstants;

import java.util.Stack;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

class ContentHandlerInfosetOutputter extends InfosetOutputter {
    protected static final char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    protected final ContentHandler contentHandler;
    protected final boolean indent;
    protected int elementLevel = 0;
    protected Stack<NamespaceBinding> namespaceBindings = new Stack<>();

    ContentHandlerInfosetOutputter(ContentHandler contentHandler, boolean indent) {
        this.contentHandler = contentHandler;
        this.indent = indent;
    }

    @Override
    public void reset() {

    }

    @Override
    public void startDocument() {
        try {
            contentHandler.startDocument();
        } catch (SAXException e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void endDocument() {
        try {
            contentHandler.endDocument();
        } catch (SAXException e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void startSimple(InfosetSimpleElement simple) {
        try {
            final AttributesImpl attributes = createAttributes(simple);
            if (simple.isNilled()) {
                attributes.addAttribute(W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "xsi:nil", "NMTOKEN", "true");
            }
            indent(elementLevel);
            contentHandler.startElement(getNamespaceUri(simple), simple.metadata().name(), getQName(simple), attributes);
            if (!simple.isNilled()) {
                contentHandler.characters(simple.getText().toCharArray(), 0, simple.getText().length());
            }
        } catch (Exception e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void endSimple(InfosetSimpleElement simple) {
        try {
            if (simple.metadata().prefix() != null) {
                namespaceBindings.pop();
            }
            contentHandler.endElement(getNamespaceUri(simple), simple.metadata().name(), getQName(simple));
        } catch (Exception e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void startComplex(InfosetComplexElement complex) {
        try {
            indent(elementLevel);
            final String nsUri = getNamespaceUri(complex);
            contentHandler.startElement(nsUri, complex.metadata().name(), getQName(complex), createAttributes(complex));
            elementLevel++;
            if (complex.isNilled()) {
                elementLevel--;
                if (complex.metadata().prefix() != null) {
                    namespaceBindings.pop();
                }
                contentHandler.endElement(nsUri, complex.metadata().name(), getQName(complex));
            }
        } catch (SAXException e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    protected AttributesImpl createAttributes(InfosetElement infosetElement) {
        final AttributesImpl attributes = new AttributesImpl();
        if (infosetElement.metadata().prefix() != null) {
            final NamespaceBinding namespaceBinding = infosetElement.metadata().minimizedScope();
            if (!namespaceBindings.contains(namespaceBinding)) {
                attributes.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceBinding.prefix(), XMLConstants.XMLNS_ATTRIBUTE + ":" + namespaceBinding.prefix(), "CDATA", namespaceBinding.uri());
            }
            namespaceBindings.push(namespaceBinding);
        }
        return attributes;
    }

    @Override
    public void endComplex(InfosetComplexElement complex) {
        try {
            elementLevel--;
            indent(elementLevel);
            if (complex.metadata().prefix() != null) {
                namespaceBindings.pop();
            }
            contentHandler.endElement(complex.metadata().namespace(), complex.metadata().name(), getQName(complex));
        } catch (SAXException e) {
            throw new ParserDfdlSmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void startArray(InfosetArray array) {
    }

    @Override
    public void endArray(InfosetArray array) {
    }

    protected String getQName(InfosetElement infosetElement) {
        final String prefix = infosetElement.metadata().prefix();
        return (prefix == null || prefix.isEmpty()) ? "" : prefix + ":" + infosetElement.metadata().name();
    }

    protected void indent(int elementLevel) throws SAXException {
        if (indent) {
            contentHandler.characters(INDENT, 0, elementLevel + 1);
        }
    }

    protected String getNamespaceUri(InfosetElement infosetElement) {
        return infosetElement.metadata().namespace() == null ? NULL_NS_URI : infosetElement.metadata().namespace();
    }
}
