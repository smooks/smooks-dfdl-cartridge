package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Resource;

@Resource(type = "dfdl-parser")
public class DfdlParserContentHandlerFactory extends AbstractDfdlContentHandlerFactory {
    @Override
    public ContentHandler doCreate(final SmooksResourceConfiguration smooksResourceConfiguration, final String dataProcessorName, final DataProcessor dataProcessor) throws SmooksConfigurationException {
        smooksResourceConfiguration.setParameter("dataProcessorName", dataProcessorName);
        smooksResourceConfiguration.setResource("org.milyn.cartridges.dfdl.DfdlParser");
        return null;
    }
}
