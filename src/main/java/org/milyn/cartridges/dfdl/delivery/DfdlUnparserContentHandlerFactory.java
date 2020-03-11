package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.milyn.cartridges.dfdl.DfdlSchema;
import org.milyn.cartridges.dfdl.DfdlUnparser;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Resource;

@Resource(type = "dfdl-unparser")
public class DfdlUnparserContentHandlerFactory extends AbstractDfdlContentHandlerFactory {
    @Override
    public ContentHandler doCreate(final SmooksResourceConfiguration smooksResourceConfiguration, final DfdlSchema dfdlSchema, final DataProcessor dataProcessor) throws SmooksConfigurationException {
        return Configurator.configure(new DfdlUnparser(dataProcessor), smooksResourceConfiguration, applicationContext);
    }
}
