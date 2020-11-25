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

import org.apache.daffodil.japi.DataLocation;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.cartridges.dfdl.AbstractTestCase;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MockApplicationContext;
import org.smooks.delivery.sax.ng.SaxNgHandler;
import org.smooks.io.StreamUtils;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DfdlParserTestCase extends AbstractTestCase {

    private StringWriter stringWriter;
    private SaxNgHandler saxHandler;

    @BeforeEach
    public void beforeEach() {
        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());
        stringWriter = new StringWriter();
        executionContext.setWriter(stringWriter);
        saxHandler = new SaxNgHandler(executionContext);
    }

    public static class ParseErrorDataProcessorFactory extends DataProcessorFactory {

        public ParseErrorDataProcessorFactory() {

        }

        @Override
        public DataProcessor createDataProcessor() {
            return new DataProcessor(null) {
                @Override
                public ParseResult parse(InputSourceDataInputStream input, InfosetOutputter output) {
                    return new ParseResult(null, null) {
                        @Override
                        public boolean isError() {
                            return true;
                        }

                        @Override
                        public List<Diagnostic> getDiagnostics() {
                            return Arrays.asList(new Diagnostic(null) {
                                @Override
                                public String getSomeMessage() {
                                    return "";
                                }

                                @Override
                                public Throwable getSomeCause() {
                                    return new Throwable();
                                }

                                @Override
                                public boolean isError() {
                                    return true;
                                }
                            });
                        }
                    };
                }
            };
        }
    }

    public static class DiagnosticErrorDataProcessorFactory extends DataProcessorFactory {

        public DiagnosticErrorDataProcessorFactory() {

        }

        @Override
        public DataProcessor createDataProcessor() {
            return new DataProcessor(null) {
                @Override
                public ParseResult parse(InputSourceDataInputStream input, InfosetOutputter output) {
                    return new ParseResult(null, null) {

                        @Override
                        public DataLocation location() {
                            return new DataLocation(null) {
                                @Override
                                public boolean isAtEnd() {
                                    return true;
                                }
                            };
                        }

                        @Override
                        public boolean isError() {
                            return false;
                        }

                        @Override
                        public List<Diagnostic> getDiagnostics() {
                            return Arrays.asList(new Diagnostic(null) {
                                @Override
                                public String getSomeMessage() {
                                    return "";
                                }

                                @Override
                                public Throwable getSomeCause() {
                                    return new Throwable();
                                }

                                @Override
                                public boolean isError() {
                                    return true;
                                }
                            });
                        }
                    };
                }
            };
        }
    }

    @Test
    public void testParseWhenParseError() throws Exception {
        ResourceConfig smooksResourceConfiguration = new ResourceConfig();
        smooksResourceConfiguration.setParameter("schemaURI", "");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(ParseErrorDataProcessorFactory.class);
        dfdlParser.setResourceConfig(smooksResourceConfiguration);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();

        assertThrows(SmooksException.class, () -> dfdlParser.parse(new InputSource(new ByteArrayInputStream("".getBytes()))));
    }

    @Test
    public void testParseWhenDiagnosticErrorButNotParseError() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setParameter("schemaURI", "");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DiagnosticErrorDataProcessorFactory.class);
        dfdlParser.setResourceConfig(resourceConfig);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(new ByteArrayInputStream("".getBytes())));

        assertEquals("", stringWriter.toString());
    }
    
    @Test
    public void testParse() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setParameter("schemaURI", "/csv.dfdl.xsd");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DataProcessorFactory.class);
        dfdlParser.setResourceConfig(resourceConfig);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8"), stringWriter.toString());
    }

    @Test
    public void testIncrementalParse() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setParameter("schemaURI", "/csv.dfdl.xsd");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DataProcessorFactory.class);
        dfdlParser.setResourceConfig(resourceConfig);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(saxHandler);
        dfdlParser.initialize();

        String input = StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8");
        dfdlParser.parse(new InputSource(new ByteArrayInputStream((input + input).getBytes())));

        String expectedResult = StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8");
        assertEquals(expectedResult + expectedResult, stringWriter.toString());
    }
    
    @Test
    public void testParseGivenIndentIsFalse() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setParameter("schemaURI", "/csv.dfdl.xsd");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DataProcessorFactory.class);
        dfdlParser.setResourceConfig(resourceConfig);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(false);
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.trimLines(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8")), stringWriter.toString());
    }
}
