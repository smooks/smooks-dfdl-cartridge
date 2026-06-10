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

import com.ibm.icu.util.Calendar;
import org.apache.daffodil.runtime1.api.DFDLPrimType;
import org.apache.daffodil.runtime1.api.InfosetSimpleElement;
import org.apache.daffodil.runtime1.api.SimpleElementMetadata;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentHandlerInfosetOutputterTestCase {

    private static class StubInfosetElement implements InfosetSimpleElement {

        private final SimpleElementMetadata elementMetadata;

        public StubInfosetElement(SimpleElementMetadata elementMetadata) {
            this.elementMetadata = elementMetadata;
        }

        @Override
        public boolean isNilled() {
            return new Random().nextBoolean();
        }

        @Override
        public SimpleElementMetadata metadata() {
            return elementMetadata;
        }

        @Override
        public String getText() {
            return "";
        }

        @Override
        public Object getAnyRef() {
            return null;
        }

        @Override
        public Object getObject() {
            return InfosetSimpleElement.super.getObject();
        }

        @Override
        public BigDecimal getDecimal() {
            return null;
        }

        @Override
        public Calendar getDate() {
            return null;
        }

        @Override
        public Calendar getTime() {
            return null;
        }

        @Override
        public Calendar getDateTime() {
            return null;
        }

        @Override
        public byte[] getHexBinary() {
            return new byte[0];
        }

        @Override
        public Boolean getBoolean() {
            return null;
        }

        @Override
        public Long getLong() {
            return 0L;
        }

        @Override
        public Integer getInt() {
            return 0;
        }

        @Override
        public Short getShort() {
            return 0;
        }

        @Override
        public Byte getByte() {
            return 0;
        }

        @Override
        public Long getUnsignedInt() {
            return 0L;
        }

        @Override
        public Integer getUnsignedShort() {
            return 0;
        }

        @Override
        public Short getUnsignedByte() {
            return 0;
        }

        @Override
        public BigInteger getUnsignedLong() {
            return null;
        }

        @Override
        public Double getDouble() {
            return 0.0;
        }

        @Override
        public Float getFloat() {
            return 0f;
        }

        @Override
        public BigInteger getInteger() {
            return null;
        }

        @Override
        public BigInteger getNonNegativeInteger() {
            return null;
        }

        @Override
        public String getString() {
            return "";
        }

        @Override
        public URI getURI() {
            return null;
        }
    }

    @Test
    public void testCreateAttributesGivenUndefinedLocalName() {
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(null, ThreadLocalRandom.current().nextBoolean());
        SimpleElementMetadata elementMetadata = new SimpleElementMetadata() {
            @Override
            public DFDLPrimType dfdlType() {
                return null;
            }

            @Override
            public String schemaFileInfo() {
                return "";
            }

            @Override
            public Long schemaFileLineNumber() {
                return 0L;
            }

            @Override
            public Long schemaFileLineColumnNumber() {
                return 0L;
            }

            @Override
            public String diagnosticDebugName() {
                return "";
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String namespace() {
                return "";
            }

            @Override
            public NamespaceBinding minimizedScope() {
                return null;
            }

            @Override
            public String prefix() {
                return null;
            }

            @Override
            public boolean isArray() {
                return false;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public String toQName() {
                return "";
            }

            @Override
            public boolean isNillable() {
                return false;
            }

            @Override
            public Map<String, String> runtimeProperties() {
                return new HashMap<>();
            }
        };

        AttributesImpl attributes = contentHandlerInfosetOutputter.createAttributes(new StubInfosetElement(elementMetadata));
        assertEquals(0, attributes.getLength());
    }

    @Test
    public void testCreateAttributesGivenLocalName() {
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(null, ThreadLocalRandom.current().nextBoolean());
        SimpleElementMetadata elementMetadata = new SimpleElementMetadata() {
            @Override
            public DFDLPrimType dfdlType() {
                return null;
            }

            @Override
            public String schemaFileInfo() {
                return "";
            }

            @Override
            public Long schemaFileLineNumber() {
                return 0L;
            }

            @Override
            public Long schemaFileLineColumnNumber() {
                return 0L;
            }

            @Override
            public String diagnosticDebugName() {
                return "";
            }

            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String namespace() {
                return "";
            }

            @Override
            public NamespaceBinding minimizedScope() {
                return new NamespaceBinding("ex", "", TopScope$.MODULE$);
            }

            @Override
            public String prefix() {
                return "";
            }

            @Override
            public boolean isArray() {
                return false;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public String toQName() {
                return "";
            }

            @Override
            public boolean isNillable() {
                return false;
            }

            @Override
            public Map<String, String> runtimeProperties() {
                return new HashMap<>();
            }
        };

        AttributesImpl attributes = contentHandlerInfosetOutputter.createAttributes(new StubInfosetElement(elementMetadata));
        assertEquals(1, attributes.getLength());
    }

    @Test
    public void testStartSimpleGivenNilledInfosetSimpleElement() {
        final Attributes[] attributes = new Attributes[1];
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(new ContentHandler() {
            @Override
            public void setDocumentLocator(Locator locator) {

            }

            @Override
            public void startDocument() throws SAXException {

            }

            @Override
            public void endDocument() throws SAXException {

            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {

            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {

            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                attributes[0] = atts;
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {

            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {

            }

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

            }

            @Override
            public void processingInstruction(String target, String data) throws SAXException {

            }

            @Override
            public void skippedEntity(String name) throws SAXException {

            }
        }, ThreadLocalRandom.current().nextBoolean());

        SimpleElementMetadata elementMetadata = new SimpleElementMetadata() {
            @Override
            public DFDLPrimType dfdlType() {
                return null;
            }

            @Override
            public String schemaFileInfo() {
                return "";
            }

            @Override
            public Long schemaFileLineNumber() {
                return 0L;
            }

            @Override
            public Long schemaFileLineColumnNumber() {
                return 0L;
            }

            @Override
            public String diagnosticDebugName() {
                return "";
            }

            @Override
            public String name() {
                return "foo";
            }

            @Override
            public String namespace() {
                return "";
            }

            @Override
            public NamespaceBinding minimizedScope() {
                return new NamespaceBinding("ex", "", TopScope$.MODULE$);
            }

            @Override
            public String prefix() {
                return "";
            }

            @Override
            public boolean isArray() {
                return false;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public String toQName() {
                return "";
            }

            @Override
            public boolean isNillable() {
                return false;
            }

            @Override
            public Map<String, String> runtimeProperties() {
                return new HashMap<>();
            }
        };

        InfosetSimpleElement stubInfosetSimpleElement = new InfosetSimpleElement() {
            @Override
            public boolean isNilled() {
                return true;
            }

            @Override
            public SimpleElementMetadata metadata() {
                return elementMetadata;
            }

            @Override
            public String getText() {
                return "";
            }

            @Override
            public Object getAnyRef() {
                return null;
            }

            @Override
            public Object getObject() {
                return InfosetSimpleElement.super.getObject();
            }

            @Override
            public BigDecimal getDecimal() {
                return null;
            }

            @Override
            public Calendar getDate() {
                return null;
            }

            @Override
            public Calendar getTime() {
                return null;
            }

            @Override
            public Calendar getDateTime() {
                return null;
            }

            @Override
            public byte[] getHexBinary() {
                return new byte[0];
            }

            @Override
            public Boolean getBoolean() {
                return null;
            }

            @Override
            public Long getLong() {
                return 0L;
            }

            @Override
            public Integer getInt() {
                return 0;
            }

            @Override
            public Short getShort() {
                return 0;
            }

            @Override
            public Byte getByte() {
                return 0;
            }

            @Override
            public Long getUnsignedInt() {
                return 0L;
            }

            @Override
            public Integer getUnsignedShort() {
                return 0;
            }

            @Override
            public Short getUnsignedByte() {
                return 0;
            }

            @Override
            public BigInteger getUnsignedLong() {
                return null;
            }

            @Override
            public Double getDouble() {
                return 0.0;
            }

            @Override
            public Float getFloat() {
                return 0f;
            }

            @Override
            public BigInteger getInteger() {
                return null;
            }

            @Override
            public BigInteger getNonNegativeInteger() {
                return null;
            }

            @Override
            public String getString() {
                return "";
            }

            @Override
            public URI getURI() {
                return null;
            }
        };

        contentHandlerInfosetOutputter.startSimple(stubInfosetSimpleElement);
        assertEquals(3, attributes[0].getLength());
        assertEquals("true", attributes[0].getValue("xsi:nil"));
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", attributes[0].getValue("xmlns:xsi"));
    }
}