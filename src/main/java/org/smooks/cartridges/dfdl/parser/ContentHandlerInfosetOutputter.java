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

import org.apache.daffodil.infoset.DIArray;
import org.apache.daffodil.infoset.DIComplex;
import org.apache.daffodil.infoset.DIElement;
import org.apache.daffodil.infoset.DISimple;
import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.smooks.api.SmooksException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import scala.Option;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import javax.xml.XMLConstants;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

class ContentHandlerInfosetOutputter extends InfosetOutputter {
    protected static final char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    protected final ContentHandler contentHandler;
    protected final boolean indent;
    protected int elementLevel = 0;

    ContentHandlerInfosetOutputter(final ContentHandler contentHandler, boolean indent) {
        this.contentHandler = contentHandler;
        this.indent = indent;
    }

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

    protected AttributesImpl createAttributes(final DIElement diElement) {
        final AttributesImpl attributes = new AttributesImpl();
        if (diElement.erd().namedQName().prefix().isDefined()) {
            final NamespaceBinding nsbStart = diElement.erd().minimizedScope();
            final NamespaceBinding nsbEnd = diElement.isRoot() ? TopScope$.MODULE$ : diElement.diParent().erd().minimizedScope();
            if (nsbStart != nsbEnd) {
                NamespaceBinding namespaceBinding = nsbStart;
                while (namespaceBinding != TopScope$.MODULE$) {
                    attributes.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceBinding.prefix(), XMLConstants.XMLNS_ATTRIBUTE + ":" + namespaceBinding.prefix(), "CDATA", namespaceBinding.uri());
                    namespaceBinding = namespaceBinding.copy$default$3();
                }
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

    protected String getQName(final DIElement diElement) {
        final Option<String> prefix = diElement.erd().namedQName().prefix();
        return (prefix.isEmpty() || prefix.get().equals("")) ? "" : prefix.get() + ":" + diElement.erd().name();
    }

    protected void indent(final int elementLevel) throws SAXException {
        if (indent) {
            contentHandler.characters(INDENT, 0, elementLevel + 1);
        }
    }

    protected String getNamespaceUri(final DIElement diElement) {
        return diElement.erd().namedQName().namespace().isNoNamespace() ? NULL_NS_URI : diElement.erd().namedQName().namespace().toString();
    }
}
