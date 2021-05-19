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

import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.cartridges.dfdl.unparser.DfdlUnparser;
import org.smooks.support.SmooksUtil;
import org.smooks.support.StreamUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FunctionalTestCase extends AbstractTestCase {

    private Smooks smooks;

    @Override
    public void doBeforeEach() {
        smooks = new Smooks();
    }

    @AfterEach
    public void afterEach() throws IOException, SAXException {
        smooks.close();
    }

    @Test
    public void testSmooksConfig() throws Exception {
        smooks.addConfigurations("/smooks-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenVariables() throws Exception {
        smooks.addConfigurations("/smooks-variables-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.tilde.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.pipe.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenCacheOnDiskAttributeIsSetToTrue() throws Exception {
        smooks.addConfigurations("/smooks-cacheOnDisk-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenDebuggingAttributeIsSetToTrue() throws Exception {
        smooks.addConfigurations("/smooks-debugging-config.xml");
        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }

    @Test
    public void testSmooksConfigGivenMissingUnparseOnNodeAttributeOnDfdlUnparser() throws Exception {
        assertThrows(SAXParseException.class, () -> smooks.addConfigurations("/smooks-missing-unparseOnNode-attribute-config.xml"));
    }

    @Test
    public void testSmooksGivenDfdlUnparserVisitor() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), ValidationMode.Full, false, false, null);
        DfdlUnparser dfdlUnparser = new DfdlUnparser(dfdlSchema.compile());

        smooks.setFilterSettings(FilterSettings.newSaxNgSettings().setDefaultSerializationOn(false));
        smooks.addVisitor(dfdlUnparser, "*");

        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.xml"), smooks);
        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), "UTF-8"), result));
    }
}
