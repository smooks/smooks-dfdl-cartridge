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
package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class DfdlSchemaTestCase extends AbstractTestCase {

    @Test
    public void testCompileHitsCacheGivenCacheOnDiskIsSetToTrue() throws Throwable {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), TestKit.getRandomItem(TestKit.getCacheOnDiskSupportedValidationModes()), true, ThreadLocalRandom.current().nextBoolean()) {
            @Override
            protected DataProcessor compileSource() throws Throwable {
                countDownLatch.countDown();
                return super.compileSource();
            }
        };
        dfdlSchema.compile();
        assertEquals(1, countDownLatch.getCount());
        dfdlSchema.compile();
        assertEquals(1, countDownLatch.getCount());
    }

    @Test
    public void testCompileGivenCacheOnDiskIsSetToTrue() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), TestKit.getRandomItem(TestKit.getCacheOnDiskSupportedValidationModes()), true, ThreadLocalRandom.current().nextBoolean());
        dfdlSchema.compile();
        assertTrue(new File(DfdlSchema.WORKING_DIRECTORY + "/csv.dfdl.xsd.dat").exists());
    }

    @Test
    public void testCompileGivenCacheOnDiskIsSetToFalse() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), ValidationMode.values()[ThreadLocalRandom.current().nextInt(ValidationMode.values().length)], false, ThreadLocalRandom.current().nextBoolean());
        dfdlSchema.compile();
        assertFalse(new File(DfdlSchema.WORKING_DIRECTORY + "/csv.dfdl.xsd.dat").exists());
    }
}
