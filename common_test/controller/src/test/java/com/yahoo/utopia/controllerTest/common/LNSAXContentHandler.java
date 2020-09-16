package org.dom4j.io;

import org.dom4j.DocumentFactory;
import org.dom4j.LElement;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class LNSAXContentHandler extends SAXContentHandler
{
  private Locator locator;

  public LNSAXContentHandler(DocumentFactory documentFactory)
  {
    super(documentFactory);
  }

  public void setDocumentLocator(Locator locator)
  {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }

  public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException
  {
    super.startElement(namespaceURI, localName, qualifiedName, attributes);
    LElement element = (LElement)getElementStack().getCurrent();
    element.setLineNumber(locator.getLineNumber());
  }
}
