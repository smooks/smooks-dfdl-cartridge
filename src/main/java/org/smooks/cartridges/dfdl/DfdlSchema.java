package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ProcessorFactory;
import scala.Predef;
import scala.collection.JavaConverters;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class DfdlSchema {

    private final URI uri;
    private final Map<String, String> variables;
    private final boolean validateSchemas;

    public DfdlSchema(final URI uri, final Map<String, String> variables, boolean validateSchemas) {
        this.uri = uri;
        this.variables = variables;
        this.validateSchemas = validateSchemas;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public boolean isValidateSchemas() {
        return validateSchemas;
    }

    public String getName() {
        return uri + ":" + validateSchemas + ":" + variables.toString();
    }

    public DataProcessor compile() throws Throwable {
        final org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        compiler.setValidateDFDLSchemas(validateSchemas);

        final ProcessorFactory processorFactory = compiler.compileSource(uri);
        if (processorFactory.isError()) {
            final List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
            throw diagnostics.get(0).getSomeCause();
        }
        final DataProcessor dataProcessor = processorFactory.onPath("/");
        if (dataProcessor.isError()) {
            final List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
            for (Diagnostic diagnostic : diagnostics) {
                throw diagnostic.getSomeCause();
            }
        }

        dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(variables).asScala().toMap(Predef.$conforms()));
        return dataProcessor;
    }
}
