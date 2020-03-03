package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ProcessorFactory;
import org.milyn.cartridges.dfdl.DFDLParser;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDFDLContentHandlerFactory implements ContentHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDFDLContentHandlerFactory.class);

    @AppContext
    protected ApplicationContext applicationContext;

    public ContentHandler create(final SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
        try {
            final String schemaURI = resourceConfig.getParameter("schemaURI").getValue();
            final DataProcessor dataProcessor = compileOrGet(schemaURI, schemaURI, resourceConfig.getBoolParameter("validateDFDLSchemas", false));
            return doCreate(resourceConfig, schemaURI, dataProcessor);
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    public abstract ContentHandler doCreate(SmooksResourceConfiguration resourceConfig, String dataProcessorName, DataProcessor dataProcessor);

    protected DataProcessor compileOrGet(final String dataProcessorName, final String schemaUri, final boolean validateDFDLSchemas) throws Exception {
        if (applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class) == null) {
            synchronized (DFDLParser.class) {
                if (applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class) == null) {
                    applicationContext.setAttribute(AbstractDFDLContentHandlerFactory.class, new ConcurrentHashMap<>());
                }
            }
        }
        final Map<String, DataProcessor> schemas = (Map<String, DataProcessor>) applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class);
        final DataProcessor dataProcessor = schemas.computeIfAbsent(dataProcessorName, k -> {
            try {
                return compileSchema(new URI(schemaUri), validateDFDLSchemas);
            } catch (URISyntaxException e) {
                throw new SmooksConfigurationException(e);
            }
        });

        return dataProcessor;
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
