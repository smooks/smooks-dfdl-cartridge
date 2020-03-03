package org.milyn.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Resource;

@Resource(type = "dfdl-parser")
public class DFDLParserContentHandlerFactory extends AbstractDFDLContentHandlerFactory {
    @Override
    public ContentHandler doCreate(final SmooksResourceConfiguration resourceConfig, final String dataProcessorName, final DataProcessor dataProcessor) throws SmooksConfigurationException {
        resourceConfig.setParameter("dataProcessorName", dataProcessorName);
        resourceConfig.setResource("org.milyn.cartridges.dfdl.DFDLParser");
        return null;
    }
}
