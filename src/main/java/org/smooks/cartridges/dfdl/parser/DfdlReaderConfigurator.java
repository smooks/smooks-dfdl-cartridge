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

import org.apache.daffodil.japi.ValidationMode;
import org.smooks.GenericReaderConfigurator;
import org.smooks.ReaderConfigurator;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksResourceConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DfdlReaderConfigurator implements ReaderConfigurator {

    protected final String schemaUri;

    protected Boolean debugging = false;
    protected Boolean cacheOnDisk = false;
    protected ValidationMode validationMode = ValidationMode.Off;
    protected Boolean indent = false;
    protected String targetProfile;
    protected Map<String, String> variables = new HashMap<>();

    public DfdlReaderConfigurator(final String schemaUri) {
        AssertArgument.isNotNullAndNotEmpty(schemaUri, "schemaUri");
        this.schemaUri = schemaUri;
    }

    public DfdlReaderConfigurator setTargetProfile(String targetProfile) {
        AssertArgument.isNotNullAndNotEmpty(targetProfile, "targetProfile");
        this.targetProfile = targetProfile;
        return this;
    }

    public DfdlReaderConfigurator setValidationMode(ValidationMode validationMode) {
        AssertArgument.isNotNull(validationMode, "validationMode");
        this.validationMode = validationMode;
        return this;
    }

    public DfdlReaderConfigurator setVariables(Map<String, String> variables) {
        AssertArgument.isNotNull(variables, "variables");
        this.variables = variables;
        return this;
    }

    public DfdlReaderConfigurator setIndent(Boolean indent) {
        AssertArgument.isNotNull(indent, "indent");
        this.indent = indent;
        return this;
    }

    protected String getDataProcessorFactory() {
        return "org.smooks.cartridges.dfdl.DataProcessorFactory";
    }

    public Boolean getDebugging() {
        return debugging;
    }

    public DfdlReaderConfigurator setDebugging(Boolean debugging) {
        AssertArgument.isNotNull(debugging, "debugging");
        this.debugging = debugging;
        return this;
    }

    public Boolean getCacheOnDisk() {
        return cacheOnDisk;
    }

    public DfdlReaderConfigurator setCacheOnDisk(Boolean cacheOnDisk) {
        AssertArgument.isNotNull(cacheOnDisk, "cacheOnDisk");
        this.cacheOnDisk = cacheOnDisk;
        return this;
    }

    @Override
    public List<SmooksResourceConfiguration> toConfig() {
        final GenericReaderConfigurator genericReaderConfigurator = new GenericReaderConfigurator(DfdlParser.class);

        genericReaderConfigurator.getParameters().setProperty("schemaURI", schemaUri);
        genericReaderConfigurator.getParameters().setProperty("validationMode", validationMode.toString());
        genericReaderConfigurator.getParameters().setProperty("cacheOnDisk", Boolean.toString(cacheOnDisk));
        genericReaderConfigurator.getParameters().setProperty("debugging", Boolean.toString(debugging));
        genericReaderConfigurator.getParameters().setProperty("indent", Boolean.toString(indent));
        genericReaderConfigurator.getParameters().setProperty("dataProcessorFactory", getDataProcessorFactory());

        final List<SmooksResourceConfiguration> smooksResourceConfigurations = genericReaderConfigurator.toConfig();
        final SmooksResourceConfiguration smooksResourceConfiguration = smooksResourceConfigurations.get(0);

        for (Map.Entry<String, String> variable : variables.entrySet()) {
            smooksResourceConfiguration.setParameter(new Parameter("variables", variable));
        }

        smooksResourceConfiguration.setTargetProfile(targetProfile);

        return smooksResourceConfigurations;
    }
}
