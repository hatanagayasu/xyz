package org.dom4j.io;

import org.dom4j.LDocumentFactory;
import org.dom4j.io.LNSAXContentHandler;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.XMLReader;

public class LNSAXReader extends SAXReader
{
  protected SAXContentHandler createContentHandler(XMLReader reader)
  {
    return new LNSAXContentHandler(new LDocumentFactory());
  }
}
