package org.dom4j;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.LElement;
import org.dom4j.QName;

public class LDocumentFactory extends DocumentFactory
{
  public Element createElement(QName qName)
  {
    return new LElement(qName);
  }
}
