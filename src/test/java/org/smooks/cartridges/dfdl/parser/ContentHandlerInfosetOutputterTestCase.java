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

import org.apache.daffodil.runtime1.api.ElementMetadata;
import org.apache.daffodil.runtime1.api.InfosetElement;
import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentHandlerInfosetOutputterTestCase {

    private static class StubInfosetElement implements InfosetElement {

        private final ElementMetadata elementMetadata;

        public StubInfosetElement(ElementMetadata elementMetadata) {
            this.elementMetadata = elementMetadata;
        }

        @Override
        public boolean isNilled() {
            return false;
        }

        @Override
        public ElementMetadata metadata() {
            return elementMetadata;
        }
    }

    @Test
    public void testCreateAttributesGivenUndefinedLocalName() {
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(null, ThreadLocalRandom.current().nextBoolean());
        ElementMetadata elementMetadata = new ElementMetadata() {
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
        ElementMetadata elementMetadata = new ElementMetadata() {
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
}
