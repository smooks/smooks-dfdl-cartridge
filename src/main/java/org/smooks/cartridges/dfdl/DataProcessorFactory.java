package org.smooks.cartridges.dfdl;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ValidationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.Config;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ApplicationContext;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataProcessorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessorFactory.class);

    @AppContext
    protected ApplicationContext applicationContext;

    @Config
    protected SmooksResourceConfiguration smooksResourceConfiguration;

    @ConfigParam(name = "schemaURI", use = ConfigParam.Use.REQUIRED)
    protected String schemaUri;

    public DataProcessor createDataProcessor() {
        try {
            final Map<String, String> variables = new HashMap<>();
            final List<Parameter> variablesParameters = smooksResourceConfiguration.getParameters("variables");
            if (variablesParameters != null) {
                for (Parameter variablesParameter : variablesParameters) {
                    final Map.Entry<String, String> variable = (Map.Entry<String, String>) variablesParameter.getObjValue();
                    variables.put(variable.getKey(), variable.getValue());
                }
            }

            final DfdlSchema dfdlSchema = new DfdlSchema(new URI(schemaUri), variables, ValidationMode.valueOf(smooksResourceConfiguration.getStringParameter("validationMode", "Off")), smooksResourceConfiguration.getBoolParameter("cacheOnDisk", false), smooksResourceConfiguration.getBoolParameter("debugging", false));
            return compileOrGet(dfdlSchema);
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    protected DataProcessor compileOrGet(final DfdlSchema dfdlSchema) {
        final ApplicationContext applicationContext = getApplicationContext();

        if (applicationContext.getAttribute(DataProcessorFactory.class) == null) {
            synchronized (DataProcessorFactory.class) {
                if (applicationContext.getAttribute(DataProcessorFactory.class) == null) {
                    applicationContext.setAttribute(DataProcessorFactory.class, new ConcurrentHashMap<>());
                }
            }
        }
        final Map<String, DataProcessor> dataProcessors = (Map<String, DataProcessor>) applicationContext.getAttribute(DataProcessorFactory.class);
        return dataProcessors.computeIfAbsent(dfdlSchema.getName(), k -> {
            LOGGER.info("Compiling and caching DFDL schema...");
            try {
                return dfdlSchema.compile();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
