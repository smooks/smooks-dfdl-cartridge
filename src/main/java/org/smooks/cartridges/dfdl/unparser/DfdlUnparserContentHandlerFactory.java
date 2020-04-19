package org.smooks.cartridges.dfdl.unparser;

import org.apache.daffodil.japi.DataProcessor;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.Configurator;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerFactory;
import org.smooks.delivery.annotation.Resource;

@Resource(type = "dfdl-unparser")
public class DfdlUnparserContentHandlerFactory implements ContentHandlerFactory {
    
    @AppContext
    protected ApplicationContext applicationContext;

    @Override
    public ContentHandler create(final SmooksResourceConfiguration smooksResourceConfiguration) throws SmooksConfigurationException {
        try {
            final String dataProcessorFactoryClassName = smooksResourceConfiguration.getStringParameter("dataProcessorFactory");
            final Class<? extends DataProcessorFactory> dataProcessorFactoryClass = (Class<? extends DataProcessorFactory>) Class.forName(dataProcessorFactoryClassName);
            final DataProcessorFactory dataProcessorFactory = dataProcessorFactoryClass.newInstance();

            Configurator.configure(dataProcessorFactory, smooksResourceConfiguration, applicationContext);
            final DataProcessor dataProcessor = dataProcessorFactory.createDataProcessor();

            return Configurator.configure(new DfdlUnparser(dataProcessor), smooksResourceConfiguration, applicationContext);
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
