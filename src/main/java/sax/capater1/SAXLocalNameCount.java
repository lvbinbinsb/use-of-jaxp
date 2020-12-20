package sax.capater1;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public class SAXLocalNameCount extends DefaultHandler {

    private Hashtable tags;

    public void startDocument() throws SAXException {
        tags = new Hashtable();
    }

    @Override
    public void endDocument() throws SAXException {
        Enumeration e = tags.keys();
        while (e.hasMoreElements()){
            String tag = (String) e.nextElement();
            int count = ((Integer) tags.get(tag)).intValue();
            System.out.println("Local Name \" " + tag + " \" occurs " + count + " times");
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String key = localName;

        Object value = tags.get(key);
        if (value==null){
            tags.put(key, 1);
        }else{
            int count = ((Integer) value).intValue();
            count++;
            tags.put(key, count);
        }
        System.out.println(uri + " : " + localName + " : " + qName);
    }

    public static void main(String[] args)throws Exception {
        String filename = null;
        for (int i = 0; i < args.length ; i++) {
            filename = args[i];
            if (i!=args.length-1){
                usage();
            }
        }
        if (filename == null){
            usage();
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader xmlReader = parser.getXMLReader();
        xmlReader.setContentHandler(new SAXLocalNameCount());
        xmlReader.parse(convertToFileURL(filename));
    }


    /*
    public void characters(char[] ch, int start, int length) throws SAXException {
        System.out.println(new String(ch, start, length));
        super.characters(ch, start, length);
    }*/

    private static String convertToFileURL(String filename){
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar!='/'){
            path = path.replace(File.separatorChar,'/');
        }
        if (!path.startsWith("/")){
            path = "/" + path;
        }
        return "file:"+path;
    }

    private static void usage(){
        System.err.println("Usage: SAXLocalNameCount <file.xml>");
        System.err.println("    -usage or -help = this message");
        System.exit(1);
    }

}
