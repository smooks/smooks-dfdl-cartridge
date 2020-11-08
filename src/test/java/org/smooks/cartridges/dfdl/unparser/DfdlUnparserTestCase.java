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
import org.junit.jupiter.api.Test;
import org.smooks.cartridges.dfdl.AbstractTestCase;
import org.smooks.container.MockExecutionContext;
import org.smooks.io.NullWriter;
import org.smooks.io.StreamUtils;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import scala.Predef;
import scala.collection.JavaConverters;

import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DfdlUnparserTestCase extends AbstractTestCase {

    @Test
    public void testVisitAfter() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");
        dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(new HashMap<String, String>() {{
            this.put("{http://example.com}Delimiter", ",");
        }}).asScala().toMap(Predef.$conforms()));

        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("/data/simpleCSV.xml"));

        DfdlUnparser dfdlUnparser = new DfdlUnparser(dataProcessor);
        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        StringWriter stringWriter = new StringWriter();
        mockExecutionContext.setWriter(new NullWriter(stringWriter));
        dfdlUnparser.visitAfter(document.getDocumentElement(), mockExecutionContext);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), stringWriter.toString()));
    }

}
