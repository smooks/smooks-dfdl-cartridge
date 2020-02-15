package org.milyn.dfdl;

import org.milyn.GenericReaderConfigurator;
import org.milyn.ReaderConfigurator;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksResourceConfiguration;

import java.util.List;

public class DFDLReaderConfigurator implements ReaderConfigurator {
    private String schemaUri;
    private String validateDFDLSchemas;

    public void setSchemaUri(String schemaUri) {
        AssertArgument.isNotNullAndNotEmpty(schemaUri, "schemaUri");
        this.schemaUri = schemaUri;
    }

    public void setValidateDFDLSchemas(String validateDFDLSchemas) {
        AssertArgument.isNotNullAndNotEmpty(validateDFDLSchemas, "validateDFDLSchemas");
        this.validateDFDLSchemas = validateDFDLSchemas;
    }

    @Override
    public List<SmooksResourceConfiguration> toConfig() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(DFDLReader.class);
        configurator.getParameters().setProperty("schemaUri", schemaUri);
        configurator.getParameters().setProperty("validateDFDLSchemas", validateDFDLSchemas);

        return configurator.toConfig();
    }


}
