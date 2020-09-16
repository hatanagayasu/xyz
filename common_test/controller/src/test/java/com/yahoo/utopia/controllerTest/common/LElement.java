package org.dom4j;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

public class LElement extends DefaultElement
{
  private int lineNumber;

  public LElement(String name)
  {
    super(name);
  }

  public LElement(QName qName)
  {
    super(qName);
  }

  public Object clone()
  {
    LElement element = (LElement)super.clone();

    if(element != this)
      element.lineNumber = lineNumber;

    return element;
  }

  protected Element createElement(String name)
  {
    LElement element = (LElement)getDocumentFactory().createElement(name);
    element.setLineNumber(getLineNumber());

    return element;
  }

  protected Element createElement(QName qName)
  {
    LElement element = (LElement)getDocumentFactory().createElement(qName);
    element.setLineNumber(getLineNumber());

    return element;
  }

  public int getLineNumber()
  {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }
}
