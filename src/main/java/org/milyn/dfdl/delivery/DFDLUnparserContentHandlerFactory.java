package org.milyn.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Resource;
import org.milyn.dfdl.DFDLUnparser;

@Resource(type = "dfdl-unparser")
public class DFDLUnparserContentHandlerFactory extends AbstractDFDLContentHandlerFactory {
    @Override
    public ContentHandler doCreate(final SmooksResourceConfiguration resourceConfig, final DataProcessor dataProcessor) throws SmooksConfigurationException {
        return Configurator.configure(new DFDLUnparser(dataProcessor), resourceConfig, applicationContext);
    }
}
