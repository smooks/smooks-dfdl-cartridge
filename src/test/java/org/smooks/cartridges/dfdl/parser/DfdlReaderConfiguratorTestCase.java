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
package org.smooks.cartridges.dfdl.parser;

import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.cartridges.dfdl.AbstractTestCase;
import org.smooks.cartridges.dfdl.TestKit;
import org.smooks.support.SmooksUtil;
import org.smooks.support.StreamUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.smooks.tck.Assertions.compareCharStreams;

public class DfdlReaderConfiguratorTestCase extends AbstractTestCase {
    @Test
    public void testToConfig() throws IOException {
        DfdlReaderConfigurator dfdlReaderConfigurator = new DfdlReaderConfigurator("/csv.dfdl.xsd").setDebugging(ThreadLocalRandom.current().nextBoolean()).setCacheOnDisk(ThreadLocalRandom.current().nextBoolean()).setIndent(ThreadLocalRandom.current().nextBoolean());
        if (dfdlReaderConfigurator.getCacheOnDisk()) {
            dfdlReaderConfigurator.setValidationMode(TestKit.getRandomItem(TestKit.getCacheOnDiskSupportedValidationModes()));
        } else {
            dfdlReaderConfigurator.setValidationMode(TestKit.getRandomItem(Arrays.asList(ValidationMode.values())));
        }

        Smooks smooks = new Smooks();
        smooks.setReaderConfig(dfdlReaderConfigurator);

        String result = SmooksUtil.filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);
        assertTrue(compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"), "UTF-8"), result));
    }
}
