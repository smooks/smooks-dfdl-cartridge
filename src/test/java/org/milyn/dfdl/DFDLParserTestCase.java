package org.milyn.dfdl;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.junit.jupiter.api.Test;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.delivery.dom.DOMBuilder;
import org.milyn.dfdl.delivery.AbstractDFDLContentHandlerFactory;
import org.milyn.io.StreamUtils;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DFDLParserTestCase {

    @Test
    public void testParse() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");

        Map<String, DataProcessor> schemas = new HashMap<>();
        schemas.put("classpath:/csv.dfdl.xsd", dataProcessor);

        ExecutionContext executionContext = new MockExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        DOMBuilder domBuilder = new DOMBuilder(executionContext);

        DFDLParser dfdlParser = new DFDLParser();
        dfdlParser.setSchemaURI("classpath:/csv.dfdl.xsd");
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(domBuilder);
        dfdlParser.getApplicationContext().setAttribute(AbstractDFDLContentHandlerFactory.class, schemas);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/simpleCSV.csv")));

        Document document = domBuilder.getDocument();
        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/simpleCSV.xml")), XmlUtil.serialize(document, true)));
    }
}
