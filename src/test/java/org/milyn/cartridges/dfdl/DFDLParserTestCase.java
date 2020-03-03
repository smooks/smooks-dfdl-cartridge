package org.milyn.cartridges.dfdl;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.junit.jupiter.api.Test;
import org.milyn.Smooks;
import org.milyn.cartridges.dfdl.delivery.AbstractDFDLContentHandlerFactory;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;
import org.milyn.delivery.sax.SAXHandler;
import org.milyn.io.StreamUtils;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.xml.sax.InputSource;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFDLParserTestCase {

    @Test
    public void testParse() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");

        Map<String, DataProcessor> dataProcessors = new HashMap<>();
        dataProcessors.put("/csv.dfdl.xsd", dataProcessor);

        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        DFDLParser dfdlParser = new DFDLParser();
        dfdlParser.setDataProcessorName("/csv.dfdl.xsd");
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(saxHandler);
        dfdlParser.getApplicationContext().setAttribute(AbstractDFDLContentHandlerFactory.class, dataProcessors);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/simpleCSV.csv")));

        assertEquals(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/simpleCSV.xml")), stringWriter.toString());
    }

    @Test
    public void testParseGivenIndentIsFalse() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");

        Map<String, DataProcessor> dataProcessors = new HashMap<>();
        dataProcessors.put("/csv.dfdl.xsd", dataProcessor);

        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        DFDLParser dfdlParser = new DFDLParser();
        dfdlParser.setDataProcessorName("/csv.dfdl.xsd");
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(false);
        dfdlParser.setContentHandler(saxHandler);
        dfdlParser.getApplicationContext().setAttribute(AbstractDFDLContentHandlerFactory.class, dataProcessors);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/simpleCSV.csv")));

        assertEquals(StreamUtils.trimLines(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/simpleCSV.xml"))), stringWriter.toString());
    }
}
