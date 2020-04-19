package org.smooks.cartridges.dfdl.parser;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MockApplicationContext;
import org.smooks.delivery.sax.SAXHandler;
import org.smooks.io.StreamUtils;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.xml.sax.InputSource;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DfdlParserTestCase {

    @Test
    public void testParse() throws Exception {
        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        SmooksResourceConfiguration smooksResourceConfiguration = new SmooksResourceConfiguration();
        smooksResourceConfiguration.setParameter("schemaURI", "/csv.dfdl.xsd");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DataProcessorFactory.class);
        dfdlParser.setSmooksResourceConfiguration(smooksResourceConfiguration);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(true);
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml")), stringWriter.toString());
    }

    @Test
    public void testParseGivenIndentIsFalse() throws Exception {
        ExecutionContext executionContext = new Smooks().createExecutionContext();
        executionContext.setAttribute(NamespaceDeclarationStack.class, new NamespaceDeclarationStack());

        StringWriter stringWriter = new StringWriter();
        SAXHandler saxHandler = new SAXHandler(executionContext, stringWriter);

        SmooksResourceConfiguration smooksResourceConfiguration = new SmooksResourceConfiguration();
        smooksResourceConfiguration.setParameter("schemaURI", "/csv.dfdl.xsd");

        DfdlParser dfdlParser = new DfdlParser();
        dfdlParser.setDataProcessorFactoryClass(DataProcessorFactory.class);
        dfdlParser.setSmooksResourceConfiguration(smooksResourceConfiguration);
        dfdlParser.setApplicationContext(new MockApplicationContext());
        dfdlParser.setIndent(false);
        dfdlParser.setContentHandler(saxHandler);

        dfdlParser.initialize();
        dfdlParser.parse(new InputSource(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")));

        assertEquals(StreamUtils.trimLines(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml"))), stringWriter.toString());
    }
}
