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
