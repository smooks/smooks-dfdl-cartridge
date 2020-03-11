package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.milyn.cartridges.dfdl.DfdlSchema;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
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
            final String schemaUri = resourceConfig.getParameter("schemaURI").getValue();
            final Map<String, String> variables = new HashMap<>();
            final List<Parameter> variablesParameters = resourceConfig.getParameters("variables");
            if (variablesParameters != null) {
                for (Parameter variablesParameter : variablesParameters) {
                    final Map.Entry<String, String> variable = (Map.Entry<String, String>) variablesParameter.getObjValue();
                    variables.put(variable.getKey(), variable.getValue());
                }
            }
            final DfdlSchema dfdlSchema = new DfdlSchema(new URI(schemaUri), variables, resourceConfig.getBoolParameter("validateDFDLSchemas", false));
            return doCreate(resourceConfig, dfdlSchema, compileOrGet(dfdlSchema));
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    public abstract ContentHandler doCreate(SmooksResourceConfiguration smooksResourceConfiguration, DfdlSchema dfdlSchema, DataProcessor dataProcessor);

    protected DataProcessor compileOrGet(final DfdlSchema dfdlSchema) {
        if (applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class) == null) {
            synchronized (AbstractDfdlContentHandlerFactory.class) {
                if (applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class) == null) {
                    applicationContext.setAttribute(AbstractDfdlContentHandlerFactory.class, new ConcurrentHashMap<>());
                }
            }
        }
        final Map<String, DataProcessor> dataProcessors = (Map<String, DataProcessor>) applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class);
        return dataProcessors.computeIfAbsent(dfdlSchema.getName(), k -> {
            LOGGER.info("Compiling and caching DFDL schema...");
            try {
                return dfdlSchema.compile();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
