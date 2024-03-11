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
package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.cartridges.dfdl.parser.DfdlParser;
import org.smooks.cartridges.dfdl.unparser.DfdlUnparser;
import org.smooks.support.SmooksUtil;
import org.smooks.support.StreamUtils;
import org.xml.sax.SAXParseException;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.smooks.tck.Assertions.compareCharStreams;

public class FunctionalTestCase extends AbstractTestCase {

    private Smooks smooks;

    @Override
    public void doBeforeEach() {
        smooks = new Smooks();
    }

    @AfterEach
    public void afterEach() {
        smooks.close();
    }

    @Test
    public void testSmooksConfig() throws Exception {
        smooks.addResourceConfigs("/smooks-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenStandaloneSchematronWhichNeverFails() throws Exception {
        smooks.addResourceConfigs("/smooks-schematron-never-fails-config.xml");
        ExecutionContext executionContext = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(executionContext, getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8"), result));
        assertNull(executionContext.get(DfdlParser.DIAGNOSTICS_TYPED_KEY));
    }

    @Test
    public void testSmooksConfigGivenStandaloneSchematronAttributeWhichAlwaysFails() throws Exception {
        smooks.addResourceConfigs("/smooks-schematron-always-fails-config.xml");
        ExecutionContext executionContext = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(executionContext, getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8"), result));
        List<Diagnostic> diagnostics = executionContext.get(DfdlParser.DIAGNOSTICS_TYPED_KEY);
        assertEquals(22, diagnostics.size());
        assertTrue(diagnostics.get(0).getMessage().startsWith("Validation Error: never fails"));
    }

    @Test
    public void testSmooksConfigGivenVariables() throws Exception {
        smooks.addResourceConfigs("/smooks-variables-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.tilde.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.pipe.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenCacheOnDiskAttributeIsSetToTrue() throws Exception {
        smooks.addResourceConfigs("/smooks-cacheOnDisk-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenDebuggingAttributeIsSetToTrue() throws Exception {
        smooks.addResourceConfigs("/smooks-debugging-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenMissingUnparseOnNodeAttributeOnDfdlUnparser() {
        assertThrows(SmooksConfigException.class, () -> smooks.addResourceConfigs("/smooks-missing-unparseOnNode-attribute-config.xml"));
    }

    @Test
    public void testSmooksGivenDfdlUnparserVisitor() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), ValidationMode.Full, false, false, null, null, false);
        DfdlUnparser dfdlUnparser = new DfdlUnparser(dfdlSchema.compile());

        smooks.setFilterSettings(FilterSettings.newSaxNgSettings().setDefaultSerializationOn(false));
        smooks.addVisitor(dfdlUnparser, "*");

        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.xml"), smooks);
        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenDistinguishedRootNode() throws Exception {
        smooks.addResourceConfigs("/smooks-distinguished-root-node-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);
        assertEquals("smith,robert,brandon,1988-03-24johnson,john,henry,1986-01-23jones,arya,cat,1986-02-19", result);
    }
}
