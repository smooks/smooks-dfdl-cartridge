package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.*;
import org.apache.daffodil.japi.debugger.TraceDebuggerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Predef;
import scala.collection.JavaConverters;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;

public class DfdlSchema {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DfdlSchema.class);
    protected static final String WORKING_DIRECTORY = ".smooks/dfdl-cartridge/";

    private final URI uri;
    private final Map<String, String> variables;
    private final ValidationMode validationMode;
    private final boolean cacheOnDisk;
    private final boolean debugging;

    public DfdlSchema(final URI uri, final Map<String, String> variables, final ValidationMode validationMode, final boolean cacheOnDisk, final boolean debugging) {
        this.uri = uri;
        this.variables = variables;
        this.validationMode = validationMode;
        this.cacheOnDisk = cacheOnDisk;
        this.debugging = debugging;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public boolean isCacheOnDisk() {
        return cacheOnDisk;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public String getName() {
        return uri + ":" + validationMode + ":" + cacheOnDisk + ":" + debugging + ":" + variables.toString();
    }

    public DataProcessor compile() throws Throwable {
        DataProcessor dataProcessor;
        if (cacheOnDisk) {
            final File binSchemaFile = new File(WORKING_DIRECTORY + new File(uri.getPath()).getName() + ".dat");
            binSchemaFile.getParentFile().mkdirs();
            if (binSchemaFile.exists()) {
                LOGGER.info("Loading compiled DFDL schema from {}", binSchemaFile.getAbsolutePath());
                dataProcessor = Daffodil.compiler().reload(binSchemaFile);
            } else {
                dataProcessor = compileSource();
                LOGGER.info("Saving compiled DFDL schema to {}", binSchemaFile.getAbsolutePath());
                dataProcessor.save(Channels.newChannel(new FileOutputStream(binSchemaFile)));
            }
        } else {
            dataProcessor = compileSource();
        }

        if (debugging) {
            dataProcessor = dataProcessor.withDebugger(new TraceDebuggerRunner()).withDebugging(true);
        }

        return dataProcessor.withValidationMode(validationMode).withExternalVariables(JavaConverters.mapAsScalaMapConverter(variables).asScala().toMap(Predef.$conforms()));
    }

    protected DataProcessor compileSource() throws Throwable {
        final org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
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

        return dataProcessor;
    }
}
