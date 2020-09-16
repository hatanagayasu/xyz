package com.yahoo.utopia.controllerTest.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.w3c.dom.NodeList;

import com.yahoo.yqatestng.common.util.Utility;
import com.yahoo.yqatestng.framework.YQA;
import com.yahoo.yqatestng.framework.wsmanager.Request;
import com.yahoo.yqatestng.framework.wsmanager.Response;
import com.yahoo.yqatestng.framework.wsmanager.testapi.HTTPTestAPI;

import yjava.ostauth.YOSTAuth;
import yjava.ostauth.YOSTAuthProvider;
import yjava.security.yca.YCA;
import yjava.security.yck.CookieJar;

public class YahooHTTPTestAPI extends HTTPTestAPI
{
  private Response response = null;
  private String uid = null;
  private String responseCode = "200";
  private HashMap parameters = new HashMap();
  private String format = "xml";

  public Response sendOauthRequest(String url, String uid, int responseCode, String schemaFile) throws Exception
  {
    List<Header> headers = new ArrayList<Header>();

    String consumerKey = YQA.getPropertyValue("YOSTAuth.consumer.key");
    YOSTAuth yoa = YOSTAuthProvider.createYOSTAuth();
    String oauthCert = yoa.internalCreateTokenWithYT(consumerKey, new CookieJar(getYahooCookies(uid)));
    Assert.assertNotNull(oauthCert, "oauthCert is null");
    headers.add(new Header("Authorization", "YahooOAuthSession " + oauthCert));

    String appid = YQA.getPropertyValue("YCA.application.identifier");
    String cert = YCA.getCertOnce(appid);
    headers.add(new Header("Yahoo-App-Auth", cert));

    Request request = getRequest();
    response = request.doGetConnect(url, null, headers, null, false);
    checkResponseCode(response, responseCode);

    if(schemaFile != null)
      verifyXMLSchema(response, Utility.getResourceFromClassLoader(schemaFile), "xml");

    return response;
  }

  public Response sendActionRequest() throws Exception
  {
    clearCookies();

    String cookieString = getBouncerCookies();
    if(uid != null)
      cookieString += getYahooCookies(uid);
    setCookies(cookieString);

    List<NameValuePair> pair = new ArrayList<NameValuePair>();
    Iterator iterator = parameters.entrySet().iterator();
    while(iterator.hasNext())
    {
      Map.Entry parameter = (Map.Entry)iterator.next();
      pair.add(new NameValuePair((String)parameter.getKey(), (String)parameter.getValue()));
    }

    List<Header> headers = new ArrayList<Header>();
    headers.add(new Header("X_REQUESTED_WITH", "XMLHttpRequest"));

    Request request = getRequest();
    String url = YQA.getPropertyValue("controller_url");
    response = request.doPostConnect(url, null, pair, headers, null, false);
    checkResponseCode(response, Integer.parseInt(responseCode));

    return response;
  }

  private void setCookies(String cookieString) throws Exception
  {
    Request request = getRequest();

    for(String cookie: cookieString.split("; "))
    {
      String[] tmp = cookie.split("=", 2);
      request.addCookie(new Cookie(".yahoo.com", tmp[0], tmp[1], "/", null, false));
    }
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

    response = logonToBouncer(null, "mps_qa", "mps_QA123");
    cookieString = cookiesToString(response.getCookies());
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

    response = logonToYahoo(null, uid, "Aa0110");
    cookieString = cookiesToString(response.getCookies());
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(cookieString);
    out.close();

    return cookieString;
  }

  private String cookiesToString(Cookie cookies[])
  {
    String cookieString = null;
    for(Cookie cookie : cookies)
    {
      if(cookieString == null)
        cookieString = cookie.toString();
      else
        cookieString += "; " + cookie.toString();
    }

    return cookieString;
  }

  public Response getResponse()
  {
    return response;
  }

  public String getResponseContentAsString()
  {
    return response.getResponseContentAsString();
  }

  public YahooHTTPTestAPI dumpResponse()
  {
    System.out.println(response.getResponseContentAsString());

    return this;
  }

  public YahooHTTPTestAPI setUid(String uid)
  {
    this.uid = uid;

    return this;
  }

  public YahooHTTPTestAPI setResponseCode(String responseCode)
  {
    this.responseCode = responseCode;

    return this;
  }

  public YahooHTTPTestAPI setMethod(String method)
  {
    parameters.put("_method_", method);

    return this;
  }

  public YahooHTTPTestAPI setFormat(String format)
  {
    this.format = format;

    return this;
  }

  public YahooHTTPTestAPI setApplication(String application)
  {
    parameters.put("_application_", application);

    return this;
  }

  public YahooHTTPTestAPI setModule(String moduleName)
  {
    parameters.put("_moduleName_", moduleName);

    return this;
  }

  public YahooHTTPTestAPI setAction(String actionName)
  {
    parameters.put("_actionName_", actionName);

    return this;
  }

  public YahooHTTPTestAPI reset()
  {
    parameters.clear();

    return this;
  }

  public String getAttribute(String name)
  {
    return (String)parameters.get("_attributes_[" + name + "]");
  }

  public YahooHTTPTestAPI addAttribute(String name, String value)
  {
    parameters.put("_attributes_[" + name + "]", value);

    return this;
  }

  public YahooHTTPTestAPI removeAttribute(String name)
  {
    parameters.remove("_attributes_[" + name + "]");

    return this;
  }

  public String getParameter(String name)
  {
    return (String)parameters.get("_parameters_[" + name + "]");
  }

  public YahooHTTPTestAPI addParameter(String name, String value)
  {
    parameters.put("_parameters_[" + name + "]", value);

    return this;
  }

  public YahooHTTPTestAPI removeParameter(String name)
  {
    parameters.remove("_parameters_[" + name + "]");

    return this;
  }

  public YahooHTTPTestAPI addCrumb() throws Exception
  {
    HashMap tmp = (HashMap)parameters.clone();
    parameters.clear();

    this.setApplication("blog")
      .setModule("publish")
      .setAction("post")
      .setMethod("GET")
      .sendActionRequest();

    parameters.clear();
    parameters.putAll(tmp);

    return addParameter("_crumb", this.getXPathValue("//crumb"));
  }

  public YahooHTTPTestAPI removeCrumb()
  {
    return removeParameter("_crumb");
  }

  public void verifyXPathExist(String xpath)
  {
    verifyXPathExist(response, xpath, format);
  }

  public void verifyXPathValue(String xpath, String value)
  {
    verifyXPathValue(response, xpath, value, format);
  }

  public void verifyXPathValueRegex(String xpath, String regex)
  {
    verifyXPathValueRegex(response, xpath, regex, format);
  }

  public void verifyXPathValues(String xpath, String value)
  {
    verifyXPathWithSameValue(response, xpath, value, format);
  }

  public void verifyXPathValues(String xpath, String values[])
  {
    String xpathValues[] = response.getXPathValueArray(xpath, format);
    Assert.assertTrue(Arrays.equals(xpathValues, values), "values of " + xpath + " is not equal to values");
  }

  public void verifyXPathValuesBelongs(String xpath, String values[])
  {
    String xpathValues[] = response.getXPathValueArray(xpath, format);
    List <String> list = Arrays.asList(values);
    for(String value : xpathValues)
      Assert.assertTrue(list.contains(value), value + " of " + xpath + " is not in values");
  }

  public void verifyXPathValuesContains(String xpath, String values[])
  {
    String xpathValues[] = response.getXPathValueArray(xpath, format);
    List <String> list = Arrays.asList(xpathValues);
    for(String value : values)
      Assert.assertTrue(list.contains(value), value + " is not in values of " + xpath);
  }

  public void verifyXPathValuesNotIn(String xpath, String value)
  {
    verifyXPathValuesNotIn(xpath, new String[]{value});
  }

  public void verifyXPathValuesNotIn(String xpath, String values[])
  {
    String xpathValues[] = response.getXPathValueArray(xpath, format);
    List <String> list = Arrays.asList(xpathValues);
    for(String value : values)
      Assert.assertFalse(list.contains(value), value + " should not be in values of " + xpath);
  }

  public void verifyXPathNodeCount(String xpath, int count)
  {
    verifyXPathNodeCount(response, xpath, count, format);
  }

  public void verifyXPathNodeCountMax(String xpath, int max)
  {
    verifyXPathNodeCountMax(response, xpath, max, format);
  }

  public void verifyXPathNodeCountMin(String xpath, int min)
  {
    verifyXPathNodeCountMin(response, xpath, min, format);
  }

  public void verifyXPathNodeCount(String xpath, int min, int max)
  {
    verifyXPathNodeCount(response, xpath, min, max, format);
  }

  public void verifyTextRegex(String regex)
  {
    verifyText(response, regex, false);
  }

  public void verifyHeaderNameExist(String headerName)
  {
    verifyHeaderNameExist(response, headerName);
  }

  public void verifyHeaderValue(String headerName, String headerValue)
  {
    verifyHeaderValue(response, headerName, headerValue);
  }

  public void verifyCookieNameExist(String cookieName)
  {
    verifyCookieNameExist(response, cookieName);
  }

  public void verifyCookieValue(String cookieName, String cookieValue)
  {
    verifyCookieValue(response, cookieName, cookieValue);
  }

  public NodeList getXPathNodeList(String xpath)
  {
    return response.getXPathNodeList(xpath, format);
  }

  public String getXPathValue(String xpath)
  {
    return response.getXPathValue(xpath, format);
  }

  public String[] getXPathValues(String xpath)
  {
    return response.getXPathValueArray(xpath, format);
  }
}
