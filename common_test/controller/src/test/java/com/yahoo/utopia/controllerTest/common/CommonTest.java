package com.yahoo.utopia.controllerTest.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;
import java.lang.Class;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;

import org.dom4j.io.LNSAXReader;
import org.dom4j.io.SAXReader;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.LElement;
import org.dom4j.Element;

import org.testng.Assert;
import org.testng.annotations.DataProvider;

//import com.yahoo.yqatestng.common.util.Utility;
//import com.yahoo.yqatestng.framework.YQA;

public class CommonTest
{
  private Properties properties;
  private Document document;
  private LElement currentTag;
  private HashMap functions;
  private HashMap variables;
  private HashMap globalVariables;
  private Stack variablesStack;
  private Stack tagStack;

  private HTTPTestAPI httpTestAPI;

  public CommonTest() throws Exception
  {
    functions = new HashMap();
    globalVariables = new HashMap();
    httpTestAPI = new HTTPTestAPI();
    properties = new Properties();
    FileInputStream in = new FileInputStream("conf/WSConf.properties");
    properties.load(in);
    in.close();
  }

  @DataProvider(name = "parseTestCase")
  public Object[][] parseTestCase(Method m) throws Exception
  {
    String tmp[] = m.getDeclaringClass().getName().split("\\.");
    String testCaseFile = tmp[tmp.length-2] + "/" + tmp[tmp.length-1] + m.getName() + ".xml";

    return parseTestCase(testCaseFile);
  }

  public Object[][] parseTestCase(String testCaseFile) throws Exception
  {
    ArrayList<Object[]> objects = new ArrayList<Object[]>();
    parse(testCaseFile, objects);

    return objects.toArray(new Object[objects.size()][]);
  }

  public void parse(String filename, ArrayList<Object[]> objects) throws Exception
  {
    File file = new File("src/test/resources/testcase/" + filename);
    LNSAXReader saxReader = new LNSAXReader();
    Document document = saxReader.read(file);
    LElement root = (LElement)document.getRootElement();

    for(LElement element : (List<LElement>)root.elements())
    {
      String tagName = element.getName();
      if(tagName.equals("testcase"))
      {
        String tcid = element.attributeValue("tcid");
        element.setAttributeValue("filename", filename);
        objects.add(new Object[] { "tcid=" + tcid, element});
      }
      else if(tagName.equals("function"))
      {
        String name = element.attributeValue("name");
        element.setAttributeValue("filename", filename);
        functions.put(name, element);
      }
      else if(tagName.equals("include"))
      {
        String includeFile = element.attributeValue("file");
        parse(includeFile, objects);
      }
    }
  }

  public void executeTestCase(Object object) throws Exception
  {
    LElement element = (LElement)object;
    String tcid = element.attributeValue("tcid");

    System.out.println("===== tcid=" + tcid + " =====");

    variables = new HashMap();
    variablesStack = new Stack();
    tagStack = new Stack();

    execute(element);
  }

  public boolean execute(LElement element) throws Exception
  {
    List<LElement> list = element.elements();
    Iterator<LElement> iterator = list.iterator();
    boolean ret = true;
    while(iterator.hasNext())
    {
      currentTag = iterator.next();
      String name = currentTag.getName();

      if(name.equals("uid"))
        variables.put("uid", getValueOrText());
      else if(name.equals("method"))
        variables.put("_method_", getValueOrText());
      else if(name.equals("application"))
        variables.put("_application_", getValueOrText());
      else if(name.equals("moduleName"))
        variables.put("_moduleName_", getValueOrText());
      else if(name.equals("actionName"))
        variables.put("_actionName_", getValueOrText());
      else if(name.equals("componentName"))
        variables.put("_componentName_", getValueOrText());
      else if(name.equals("parameters"))
        parameters();
      else if(name.equals("addParameter"))
        addParameter();
      else if(name.equals("removeParameter"))
        removeParameter();
      else if(name.equals("files"))
        files();
      else if(name.equals("sendActionRequest"))
        sendActionRequest();
      else if(name.equals("verifyXPathExist"))
        verifyXPathExist();
      else if(name.equals("verifyXPathValue"))
        verifyXPathValue();
      else if(name.equals("verifyXPathValueRegex"))
        verifyXPathValueRegex();
      else if(name.equals("verifyXPathValues"))
        verifyXPathValues();
      else if(name.equals("verifyXPathValuesBelongs"))
        verifyXPathValuesBelongs();
      else if(name.equals("verifyXPathValuesContains"))
        verifyXPathValuesContains();
      else if(name.equals("verifyXPathValuesNotIn"))
        verifyXPathValuesNotIn();
      else if(name.equals("verifyXPathNodeCount"))
        verifyXPathNodeCount();
      else if(name.equals("verifyXPathNodeCountMax"))
        verifyXPathNodeCountMax();
      else if(name.equals("verifyXPathNodeCountMin"))
        verifyXPathNodeCountMin();
      else if(name.equals("verifyTextRegex"))
        verifyTextRegex();
      else if(name.equals("verifyHeaderNameExist"))
        verifyHeaderNameExist();
      else if(name.equals("verifyHeaderValue"))
        verifyHeaderValue();
      else if(name.equals("verifyCookieNameExist"))
        verifyCookieNameExist();
      else if(name.equals("verifyCookieValue"))
        verifyCookieValue();
      else if(name.equals("getXPathValue"))
        getXPathValue();
      else if(name.equals("getXPathValues"))
        getXPathValues();
      else if(name.equals("getTextValue"))
        getTextValue();
      else if(name.equals("replace"))
        replace();
      else if(name.equals("assign"))
        assign();
      else if(name.equals("append"))
        append();
      else if(name.equals("execute"))
        execute();
      else if(name.equals("return"))
        ret = _return();
      else if(name.equals("if"))
        ret = _if();
      else if(name.equals("foreach"))
        ret = _foreach();
      else if(name.equals("push"))
        push();
      else if(name.equals("pop"))
        pop();
      else if(name.equals("dump"))
        dump();
      else if(name.equals("dumpResponse"))
        System.out.println(httpTestAPI.getResponseContentAsString());
      else
        fail("is a unknown tag");

      if(!ret)
        return false;
    }

    return true;
  }

  private String getValueOrText()
  {
    if(currentTag.attribute("value") != null)
      return expandVariable(currentTag.attributeValue("value"));
    else
      return expandVariable(currentTag.getText());
  }

  private String getAttributeValue(String name)
  {
    String value = currentTag.attributeValue(name);

    return expandVariable(value);
  }

  private String expandVariable(String value)
  {
    if(value.startsWith("$"))
    {
      value = value.substring(1, value.length());
      value = expandVariable(value);

      if(value.startsWith("g_"))
      {
        assertTrue(globalVariables.containsKey(value), "getAttributeValue");
        value = (String)globalVariables.get(value);
      }
      else
      {
        assertTrue(variables.containsKey(value), "getAttributeValue");
        value = (String)variables.get(value);
      }
    }
    else
    {
      String regex = "\\{(\\$[a-zA-Z_][a-zA-Z_0-9]*)}";
      Pattern pattern = Pattern.compile(regex);
      while(true)
      {
        Matcher matcher = pattern.matcher(value);
        if(!matcher.find())
          break;

        value = matcher.group(1);
        value = expandVariable(value);
        value = matcher.replaceAll(value);
      }
    }

    return value;
  }

  private List<String> getAttributeValues(String name)
  {
    String key = currentTag.attributeValue(name);
    List<String> values = (List<String>)(key.startsWith("g_") ?
      globalVariables.get(key) : variables.get(key));

    return values;
  }

  private void parameters()
  {
    List<LElement> list = currentTag.elements("parameter");
    Iterator<LElement> iterator = list.iterator();
    while(iterator.hasNext())
    {
      currentTag = iterator.next();
      String name = getAttributeValue("name");
      String value = getValueOrText();
      variables.put("_parameters_[" + name + "]", value);
    }
  }

  private void addParameter()
  {
    String name = getAttributeValue("name");
    String value = getAttributeValue("value");
    variables.put("_parameters_[" + name + "]", value);
  }

  private void removeParameter()
  {
    String name = getAttributeValue("name");
    variables.remove("_parameters_[" + name + "]");
  }

  private void files()
  {
    List<LElement> list = currentTag.elements("file");
    Iterator<LElement> iterator = list.iterator();
    while(iterator.hasNext())
    {
      currentTag = iterator.next();
      String name = getAttributeValue("name");
      String value = getValueOrText();
      variables.put("_files_[" + name + "]", value);
    }
  }

  private void sendActionRequest() throws Exception
  {
    int responseCode = currentTag.attribute("responseCode") == null ?
      200 : Integer.parseInt(getAttributeValue("responseCode"));
    String url = getPropertyValue("controller_url");

    List<Header> headers = new ArrayList<Header>();
    headers.add(new Header("X_REQUESTED_WITH", "XMLHttpRequest"));

    String cookieString = getBouncerCookies();
    if(variables.containsKey("uid"))
      cookieString += getYahooCookies((String)variables.get("uid"));
    List<Cookie> cookies = stringToCookies(cookieString);

    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    for(Object key : variables.keySet())
    {
      String name = (String)key;
      if(name.startsWith("_parameters_["))
      {
        String value = (String)variables.get(name);
        parameters.add(new NameValuePair(name, value));
        name = name.substring(13, name.length() - 1);
        parameters.add(new NameValuePair(name, value));
      }
      else if(name.matches("^_.*_$"))
      {
        String value = (String)variables.get(name);
        parameters.add(new NameValuePair(name, value));
      }
    }

    List<NameValuePair>files = new ArrayList<NameValuePair>();
    for(Object key : variables.keySet())
    {
      String name = (String)key;
      if(name.startsWith("_files_["))
      {
        String value = (String)variables.get(name);
        name = name.substring(8, name.length() - 1);
        files.add(new NameValuePair(name, value));
      }
    }

    httpTestAPI.doPostConnect(url, headers, cookies, parameters, files);

    assertEquals(httpTestAPI.getResponseCode(), responseCode, "Response Code");

    document = null;
    String content = httpTestAPI.getResponseContentAsString();
    String regex = "<\\?xml.*</DATA>";
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    if(matcher.find())
    {
      SAXReader saxReader = new SAXReader();
      document = saxReader.read(new StringReader(matcher.group(0)));
    }
  }

  private List<Cookie> stringToCookies(String cookieString) throws Exception
  {
    List<Cookie> cookies = new ArrayList<Cookie>();

    for(String cookie: cookieString.split("; "))
    {
      String[] tmp = cookie.split("=", 2);
      cookies.add(new Cookie(".yahoo.com", tmp[0], tmp[1], "/", null, false));
    }

    return cookies;
  }

  private String getBouncerCookies() throws Exception
  {
    String cookieFile = "/tmp/Bouncer_cookies.txt";
    String cookieString = null;

    File file = new File(cookieFile);
    if(file.exists())
    {
      Calendar cal = Calendar.getInstance();
      if(cal.getTime().getTime() - file.lastModified() < 3600000)
      {
        BufferedReader in = new BufferedReader(new FileReader(file));
        cookieString = in.readLine();
        in.close();

        return cookieString;
      }
    }

    //httpTestAPI.logonToBouncer("mps_qa", "mps_QA123");
    httpTestAPI.logonToBouncer("hata", "e04su3su;6hH");
    cookieString = cookiesToString(httpTestAPI.getCookies());
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(cookieString);
    out.close();

    return cookieString;
  }

  private String getYahooCookies(String uid) throws Exception
  {
    String cookieFile = "/tmp/" + uid + "_cookies.txt";
    String cookieString = null;

    File file = new File(cookieFile);
    if(file.exists())
    {
      Calendar cal = Calendar.getInstance();
      if(cal.getTime().getTime() - file.lastModified() < 3600000)
      {
        BufferedReader in = new BufferedReader(new FileReader(file));
        cookieString = in.readLine();
        in.close();

        return cookieString;
      }
    }

    httpTestAPI.logonToYahoo(uid, "Aa0110");
    cookieString = cookiesToString(httpTestAPI.getCookies());
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(cookieString);
    out.close();

    return cookieString;
  }

  private String cookiesToString(Cookie cookies[])
  {
    String cookieString = "";
    for(Cookie cookie : cookies)
      cookieString += cookie.getName() + "=" + cookie.getValue() + "; ";

    return cookieString;
  }

  private void verifyXPathExist()
  {
    String xpath = getAttributeValue("xpath");
    List list = getXPathNodeList(xpath);

    assertTrue(list.size() > 0, xpath);
  }

  private void verifyXPathValue()
  {
    String xpath = getAttributeValue("xpath");
    String value = getXPathValue(xpath);

    assertEquals(value, getAttributeValue("value"), xpath);
  }

  private void verifyXPathValues()
  {
    if(currentTag.attribute("value") != null)
      verifyXPathValues(getAttributeValue("value"));
    else
      verifyXPathValues(getAttributeValues("values"));
  }

  private void verifyXPathValues(String value)
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);

    for(int i = 0; i < xpathValues.size(); i++)
      assertEquals(xpathValues.get(i), value, xpath + "[" + i + "]");
  }

  private void verifyXPathValues(List<String> values)
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);

    //FIXME
    //assertEquals(xpathValues, values, xpath);
  }

  private void verifyXPathValueRegex()
  {
    String xpath = getAttributeValue("xpath");
    String regex = getAttributeValue("regex");
    String value = getXPathValue(xpath);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(value);

    assertTrue(matcher.find(), xpath + " value:" + value + " with regex:" + regex);
  }

  private void verifyXPathValuesBelongs()
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);
    List<String> values = getAttributeValues("values");

    for(int i = 0; i < xpathValues.size(); i++)
    {
      assertTrue(values.contains(xpathValues.get(i)),
        "value of " + xpath + "[" + i + "]=" + xpathValues.get(i) + " is not in values");
    }
  }

  private void verifyXPathValuesContains()
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);
    List<String> values = getAttributeValues("values");

    for(int i = 0; i < values.size(); i++)
    {
      assertTrue(xpathValues.contains(values.get(i)),
        "values[" + i + "]=" + values.get(i) + " is not in values of " + xpath);
    }
  }

  private void verifyXPathValuesNotIn()
  {
    if(currentTag.attribute("value") != null)
      verifyXPathValuesNotIn(getAttributeValue("value"));
    else
      verifyXPathValuesNotIn(getAttributeValues("values"));
  }

  public void verifyXPathValuesNotIn(String value)
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);

    assertFalse(xpathValues.contains(value),
      "value=" + value + " should not be in values of " + xpath);
  }

  public void verifyXPathValuesNotIn(List<String> values)
  {
    String xpath = getAttributeValue("xpath");
    List<String> xpathValues = getXPathValues(xpath);

    for(int i = 0; i < values.size(); i++)
    {
      assertFalse(xpathValues.contains(values.get(i)),
        "values[" + i + "]" + values.get(i) + " should not be in values of " + xpath);
    }
  }

  private void verifyXPathNodeCount()
  {
    if(currentTag.attribute("count") != null)
      verifyXPathNodeCount(Integer.parseInt(getAttributeValue("count")));
    else
      verifyXPathNodeCount(
        Integer.parseInt(getAttributeValue("min")),
        Integer.parseInt(getAttributeValue("max")));
  }

  private void verifyXPathNodeCount(int count)
  {
    String xpath = getAttributeValue("xpath");
    List list = getXPathNodeList(xpath);

    assertEquals(list.size(), count, xpath);
  }

  private void verifyXPathNodeCount(int min, int max)
  {
    String xpath = getAttributeValue("xpath");
    List list = getXPathNodeList(xpath);
    int count = list.size();

    assertTrue(count <= max, "count(" + xpath + ")=" + count + " <= " + max);
    assertTrue(count >= min, "count(" + xpath + ")=" + count + " >= " + min);
  }

  private void verifyXPathNodeCountMax()
  {
    String xpath = getAttributeValue("xpath");
    int max = Integer.parseInt(getAttributeValue("max"));
    List list = getXPathNodeList(xpath);
    int count = list.size();

    assertTrue(count <= max, "count(" + xpath + ")=" + count + " <= " + max);
  }

  private void verifyXPathNodeCountMin()
  {
    String xpath = getAttributeValue("xpath");
    int min = Integer.parseInt(getAttributeValue("min"));
    List list = getXPathNodeList(xpath);
    int count = list.size();

    assertTrue(count >= min, "count(" + xpath + ")=" + count + " >= " + min);
  }

  private void verifyTextRegex()
  {
    String regex = getAttributeValue("regex");
    String content = httpTestAPI.getResponseContentAsString();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(content);

    assertTrue(matcher.find(), "regex: "+ regex + " in the response content");
  }

  private void verifyHeaderNameExist()
  {
    String name = getAttributeValue("name");
    Header[] headers = httpTestAPI.getResponseHeaders();

    for(Header header : headers)
    {
      if(name.equals(header.getName()))
        return;
    }
    fail(name + " Exists");
  }

  private void verifyHeaderValue()
  {
    String name = getAttributeValue("name");
    String value = getAttributeValue("value");
    Header[] headers = httpTestAPI.getResponseHeaders();

    for(Header header : headers)
    {
      if(name.equals(header.getName()))
        assertEquals(header.getValue(), value, name);
    }
    fail(name + " Exists");
  }

  private void verifyCookieNameExist()
  {
    String name = getAttributeValue("name");
    Cookie[] cookies = httpTestAPI.getCookies();

    for(Cookie cookie : cookies)
    {
      if(name.equals(cookie.getName()))
        return;
    }
    fail(name + " Exists");
  }

  private void verifyCookieValue()
  {
    String name = getAttributeValue("name");
    String value = getAttributeValue("value");
    Cookie[] cookies = httpTestAPI.getCookies();

    for(Cookie cookie : cookies)
    {
      if(name.equals(cookie.getName()))
        assertEquals(cookie.getValue(), value, name);
    }
    fail(name + " Exists");
  }

  private void getXPathValue()
  {
    String xpath = getAttributeValue("xpath");
    String value = getXPathValue(xpath);
    String assign = getAttributeValue("assign");
    assign(assign, value);
  }

  private String getXPathValue(String xpath)
  {
    assertNotNull(document, xpath + " Exists");
    Element element = (Element)document.selectSingleNode(xpath);
    assertNotNull(element, xpath + " Exists");
    String value = element.getText();

    return value;
  }

  private void getXPathValues()
  {
    String xpath = getAttributeValue("xpath");
    List<String> values = getXPathValues(xpath);
    String assign = getAttributeValue("assign");
    assign(assign, values);
  }

  private List<String> getXPathValues(String xpath)
  {
    List list = getXPathNodeList(xpath);
    List values = new ArrayList<String>();
    for(Element element : (List<Element>)list)
      values.add(element.getText());

    return values;
  }

  private List getXPathNodeList(String xpath)
  {
    assertNotNull(document, xpath + " Exists");
    List list = document.selectNodes(xpath);
    assertNotNull(list, xpath + " Exists");

    return list;
  }

  private void getTextValue()
  {
    String name = getAttributeValue("assign");
    String value = httpTestAPI.getResponseContentAsString();
    assign(name, value);
  }

  private void replace()
  {
    String regex = getAttributeValue("regex");
    String replace = getAttributeValue("replace");
    String value = getAttributeValue("value");
    String name = getAttributeValue("assign");

    value = value.replaceAll(regex, replace);
    assign(name, value);
  }

  private void assign()
  {
    String name = getAttributeValue("name");
    if(currentTag.attribute("value") != null)
      assign(name, getAttributeValue("value"));
    else
      assign(name, getAttributeValues("values"));
  }

  private void assign(String name, String value)
  {
    if(currentTag.attribute("regex") != null &&
      currentTag.attribute("group") != null)
    {
      String regex = getAttributeValue("regex");
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(value);
      if(matcher.find())
      {
        int group = Integer.parseInt(getAttributeValue("group"));
        value = matcher.group(group);
      }
      else
      {
       value = null;
      }
    }

    if(name.startsWith("g_"))
      globalVariables.put(name, value);
    else
      variables.put(name, value);
  }

  private void assign(String name, List<String> values)
  {
    if(name.startsWith("g_"))
      globalVariables.put(name, values);
    else
      variables.put(name, values);
  }

  private void append()
  {
    String name = getAttributeValue("name");
    if(currentTag.attribute("value") != null)
      append(name, getAttributeValue("value"));
    else
      append(name, getAttributeValues("values"));
  }

  private void append(String name, String value)
  {
    List<String> list = variables.containsKey(name) ?
      (List<String>)variables.get(name) : new ArrayList<String>();
    list.add(value);

    if(name.startsWith("g_"))
      globalVariables.put(name, list);
    else
      variables.put(name, list);
  }

  private void append(String name, List<String> values)
  {
    List<String> list = variables.containsKey(name) ?
      (List<String>)variables.get(name) : new ArrayList<String>();
    list.addAll(values);

    if(name.startsWith("g_"))
      globalVariables.put(name, list);
    else
      variables.put(name, list);
  }

  private void execute() throws Exception
  {
    String name = getAttributeValue("name");
    String value;
    LElement element = (LElement)functions.get(name);
    boolean inline = false;
    HashMap variables;

    assertNotNull(element, "function " + name);

    if(element.attribute("type") != null && element.attributeValue("type").equals("inline"))
      inline = true;

    if(inline)
    {
      variables = (HashMap)this.variables.clone();

      for(Attribute attribute : (List<Attribute>)currentTag.attributes())
      {
        name = attribute.getName();
        value = getAttributeValue(name);
        this.variables.put(name, value);
      }
    }
    else
    {
      variables = new HashMap();

      for(Attribute attribute : (List<Attribute>)currentTag.attributes())
      {
        name = attribute.getName();
        value = getAttributeValue(name);
        variables.put(name, value);
      }

      variablesStack.push(this.variables);
      this.variables = variables;
    }

    tagStack.push(currentTag);
    execute(element);
    currentTag = (LElement)tagStack.pop();

    if(inline)
    {
      for(Attribute attribute : (List<Attribute>)currentTag.attributes())
      {
        name = attribute.getName();
        if(variables.containsKey(name))
          this.variables.put(name, variables.get(name));
        else
          this.variables.remove(name);
      }
    }
    else
    {
      variables = this.variables;
      this.variables = (HashMap)variablesStack.pop();

      if(variables.containsKey("assign") && variables.containsKey("return"))
      {
        name = (String)variables.get("assign");
        value = (String)variables.get("return");
        assign(name, value);
      }
    }
  }

  private boolean _return()
  {
    if(currentTag.attribute("value") != null)
      variables.put("return", getAttributeValue("value"));

    return false;
  }

  private boolean _if() throws Exception
  {
    if(currentTag.attribute("xpath") != null)
    {
      String xpath = getAttributeValue("xpath");
      List list = document.selectNodes(xpath);

      if(list.size() > 0)
        return execute(currentTag);
    }
    else if(currentTag.attribute("var") != null)
    {
      String var = currentTag.attributeValue("var");
      if(var.startsWith("$"))
      {
        var = var.substring(1, var.length());
        var = expandVariable(var);
      }

      if(var.startsWith("g_"))
      {
        if(globalVariables.containsKey(var))
          return execute(currentTag);
      }
      else
      {
        if(variables.containsKey(var))
          return execute(currentTag);
      }
    }

    return true;
  }

  private boolean _foreach() throws Exception
  {
    List<String> from = getAttributeValues("from");
    String item = getAttributeValue("item");
    for(String value : from)
    {
      assign(item, value);
      if(!execute(currentTag))
        return false;
    }
    variables.remove("from");
    variables.remove("item");

    return true;
  }

  private void push()
  {
    HashMap variables = (HashMap)this.variables.clone();

    variablesStack.push(this.variables);
    this.variables = variables;
  }

  private void pop()
  {
    this.variables = (HashMap)variablesStack.pop();
  }

  private void dump()
  {
    System.out.println("dump:");
    if(currentTag.attribute("name") != null)
    {
      String name = currentTag.attributeValue("name");
      if(name.startsWith("$"))
        name = name.substring(1, name.length());
      name = expandVariable(name);
      dump(name.startsWith("g_") ? globalVariables : variables, name);
    }
    else
    {
      for(Object name : globalVariables.keySet())
        dump(globalVariables, (String)name);
      for(Object name : variables.keySet())
        dump(variables, (String)name);
    }
  }

  private void dump(HashMap variables, String name)
  {
    Object object = variables.get(name);
    if(object == null)
    {
      System.out.println(name + " Not Exists");
    }
    else if(object instanceof String)
    {
      System.out.println(name + "=" + object);
    }
    else
    {
      List<String> a = (ArrayList<String>)object;
      String result = name + "=";
      if(a.size() > 0)
      {
        result += a.get(0);
        for(int i = 1; i < a.size(); i++)
          result += "," + a.get(i);
      }

      System.out.println(result);
    }
  }

  private String dumpTag(LElement tag)
  {
    LElement parentTag = tag;

    do
      parentTag = (LElement)parentTag.getParent();
    while(parentTag.attribute("filename") == null);

    String result = "  " + parentTag.attributeValue("filename") +
      " <" + parentTag.getName();
    if(parentTag.attributes() != null)
    {
      for(Attribute attribute : (List<Attribute>)parentTag.attributes())
      {
        if(!attribute.getName().equals("filename"))
          result += " " + attribute.getName() + "=\"" + attribute.getValue() + "\"";
      }
    }
    result += ">\n";

    result += "  Line:" + tag.getLineNumber() + " <" + tag.getName();
    if(tag.attributes() != null)
    {
      for(Attribute attribute : (List<Attribute>)tag.attributes())
        result += " " + attribute.getName() + "=\"" + attribute.getValue() + "\"";
    }
    result += ">\n";

    return result;
  }

  private String __LINE__()
  {
    String result = "\n";
    int level = 1;

    tagStack.push(currentTag);
    while(!tagStack.empty())
    {
      LElement tag = (LElement)tagStack.pop();
      result += "#" + level + "\n" + dumpTag(tag);
      level++;
    }

    return result;
  }

  private void fail(String message)
  {
    Assert.fail(message + __LINE__());
  }

  private String format(Object actual, Object expected, String message)
  {
    String formatted = "";
    if(null != message)
      formatted = message + " ";

    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  private void failNotEquals(Object actual , Object expected, String message)
  {
    fail(format(actual, expected, message));
  }

  private void assertTrue(boolean condition, String message)
  {
    if(!condition)
      failNotEquals(Boolean.valueOf(condition), Boolean.TRUE, message);
  }

  private void assertFalse(boolean condition, String message)
  {
    if(condition)
      failNotEquals(Boolean.valueOf(condition), Boolean.FALSE, message);
  }

  private void assertNotNull(Object object, String message)
  {
    assertTrue(object != null, message);
  }

  private void assertEquals(Object actual, Object expected, String message)
  {
    if((expected == null) && (actual == null))
      return;
    if((expected != null) && expected.equals(actual))
      return;
    failNotEquals(actual, expected, message);
  }

  private void assertEquals(int actual,  int expected, String message)
  {
    assertEquals(new Integer(actual), new Integer(expected), message);
  }

  public String getPropertyValue(String name)
  {
    return (String)properties.get(name);
  }
}
