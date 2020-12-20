package sax.capater1;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.PrintStream;

public class MyErrorHandler implements ErrorHandler {
    private PrintStream out;

    private String getParseExceptionInfo(SAXParseException spe){
        String systemId = spe.getSystemId();
        if (systemId==null){
            systemId="null";
        }
        String info = "URI=" + systemId + " Line=" + spe.getLineNumber() + " :" + spe.getMessage();
        return  info;
    }

    public MyErrorHandler(PrintStream out) {
        this.out = out;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        out.println("Warning: " + getParseExceptionInfo(exception));
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        String message = "Error: " + getParseExceptionInfo(exception);
        throw new SAXException(message);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        String message = "Fatal Error: " + getParseExceptionInfo(exception);
        throw new SAXException(message);
    }
}
