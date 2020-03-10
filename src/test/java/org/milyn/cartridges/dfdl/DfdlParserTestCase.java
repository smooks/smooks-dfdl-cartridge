package org.milyn.cartridges.dfdl;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.junit.jupiter.api.Test;
import org.milyn.Smooks;
import org.milyn.cartridges.dfdl.delivery.AbstractDfdlContentHandlerFactory;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;
import org.milyn.delivery.sax.SAXHandler;
import org.milyn.io.StreamUtils;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.xml.sax.InputSource;
import scala.Predef;
import scala.collection.JavaConverters;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DfdlParserTestCase {

    @Test
    public void testParse() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");

        Map<String, DataProcessor> dataProcessors = new HashMap<>();
        dataProcessors.put("foo", dataProcessor);
        dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(new HashMap<String, String>(){{
            this.put("{http://example.com}Delimiter", ",");
        }}).asScala().toMap(Predef.$conforms()));

        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorName("foo");
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(saxHandler);
        dfdlParser.getApplicationContext().setAttribute(AbstractDfdlContentHandlerFactory.class, dataProcessors);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml")), stringWriter.toString());
    }

    @Test
    public void testParseGivenIndentIsFalse() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");
        dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(new HashMap<String, String>(){{
            this.put("{http://example.com}Delimiter", ",");
        }}).asScala().toMap(Predef.$conforms()));

        Map<String, DataProcessor> dataProcessors = new HashMap<>();
        dataProcessors.put("foo", dataProcessor);

        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorName("foo");
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(false);
        dfdlParser.setContentHandler(saxHandler);
        dfdlParser.getApplicationContext().setAttribute(AbstractDfdlContentHandlerFactory.class, dataProcessors);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.trimLines(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"))), stringWriter.toString());
    }
}
