package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ProcessorFactory;
import org.milyn.cartridges.dfdl.DfdlParser;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Predef;
import scala.collection.JavaConverters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDfdlContentHandlerFactory implements ContentHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDfdlContentHandlerFactory.class);

    @AppContext
    protected ApplicationContext applicationContext;

    public ContentHandler create(final SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
        try {
            final String schemaURI = resourceConfig.getParameter("schemaURI").getValue();
            final Map<String, String> variables = new HashMap<>();
            final List<Parameter> variablesParameters = resourceConfig.getParameters("variables");
            final StringBuilder dataProcessorNameStringBuilder = new StringBuilder(schemaURI);
            if (variablesParameters != null) {
                for (Parameter variablesParameter : variablesParameters) {
                    Map.Entry<String, String> variable = (Map.Entry<String, String>) variablesParameter.getObjValue();
                    variables.put(variable.getKey(), variable.getValue());
                    dataProcessorNameStringBuilder.append(":").append(variable.getKey()).append("=").append(variable.getValue());
                }
            }
            boolean validateDFDLSchemas = resourceConfig.getBoolParameter("validateDFDLSchemas", false);
            dataProcessorNameStringBuilder.append(":").append("validateDFDLSchemas").append("=").append(validateDFDLSchemas);

            final String dataProcessorName = dataProcessorNameStringBuilder.toString();
            final DataProcessor dataProcessor = compileOrGet(dataProcessorName, schemaURI, validateDFDLSchemas, variables);
            return doCreate(resourceConfig, dataProcessorName, dataProcessor);
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    public abstract ContentHandler doCreate(SmooksResourceConfiguration smooksResourceConfiguration, String dataProcessorName, DataProcessor dataProcessor);

    protected DataProcessor compileOrGet(final String dataProcessorName, final String schemaUri, final boolean validateDFDLSchemas, final Map<String, String> variables) {
        if (applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class) == null) {
            synchronized (DfdlParser.class) {
                if (applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class) == null) {
                    applicationContext.setAttribute(AbstractDfdlContentHandlerFactory.class, new ConcurrentHashMap<>());
                }
            }
        }
        final Map<String, DataProcessor> schemas = (Map<String, DataProcessor>) applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class);
        return schemas.computeIfAbsent(dataProcessorName, k -> {
            try {
                final DataProcessor dataProcessor = compileSchema(new URI(schemaUri), validateDFDLSchemas);
                dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(variables).asScala().toMap(Predef.$conforms()));
                return dataProcessor;
            } catch (URISyntaxException e) {
                throw new SmooksConfigurationException(e);
            }
        });
    }

    protected DataProcessor compileSchema(final URI uri, final boolean validateDFDLSchemas) {
        try {
            LOGGER.info("Compiling and caching DFDL schema...");
            final org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
            compiler.setValidateDFDLSchemas(validateDFDLSchemas);

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
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
