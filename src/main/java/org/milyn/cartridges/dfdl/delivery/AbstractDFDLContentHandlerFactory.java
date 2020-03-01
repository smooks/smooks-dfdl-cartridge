package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ProcessorFactory;
import org.milyn.SmooksException;
import org.milyn.cartridges.dfdl.DFDLParser;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerFactory;
import org.milyn.resource.URIResourceLocator;
import org.milyn.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDFDLContentHandlerFactory implements ContentHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDFDLContentHandlerFactory.class);

    @AppContext
    protected ApplicationContext applicationContext;

    public ContentHandler create(final SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
        try {

            if (applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class) == null) {
                synchronized (DFDLParser.class) {
                    if (applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class) == null) {
                        applicationContext.setAttribute(AbstractDFDLContentHandlerFactory.class, new ConcurrentHashMap<>());
                    }
                }
            }
            final Map<String, DataProcessor> schemas = (Map<String, DataProcessor>) applicationContext.getAttribute(AbstractDFDLContentHandlerFactory.class);
            final DataProcessor dataProcessor = schemas.computeIfAbsent(resourceConfig.getParameter("schemaURI").getValue(), k -> compileSchema(k, resourceConfig.getBoolParameter("validateDFDLSchemas", false)));

            return doCreate(resourceConfig, dataProcessor);
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    public abstract ContentHandler doCreate(SmooksResourceConfiguration resourceConfig, DataProcessor dataProcessor) throws SmooksConfigurationException;

    protected DataProcessor compileSchema(final String uriAsString, final boolean validateDFDLSchemas) {
        try {
            final URI uri = URI.create(uriAsString);
            final URI newUri;
            if (uri.getScheme().equals(URIResourceLocator.SCHEME_CLASSPATH)) {
                final List<URL> resources = ClassUtil.getResources(uri.getRawSchemeSpecificPart(), DFDLParser.class);
                if (resources.isEmpty()) {
                    final IOException ioException = new IOException("Failed to read DFDL schema: " + uri);
                    throw new SmooksException(ioException.getMessage(), ioException);
                } else {
                    newUri = resources.get(0).toURI();
                }
            } else {
                newUri = uri;
            }

            LOGGER.info("Compiling and caching DFDL schema...");
            final org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
            compiler.setValidateDFDLSchemas(validateDFDLSchemas);

            final ProcessorFactory processorFactory = compiler.compileSource(newUri);
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
