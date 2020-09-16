package com.yahoo.utopia.controllerTest.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

class HTTPTestAPI
{
  private HttpClient client;
  private int responseCode;
  private Header[] headers;
  private Cookie[] cookies;
  private String content;

  public HTTPTestAPI()
  {
    client = new HttpClient();
    client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
  }

  public void doPostConnect(String url, List<Header> headers, List<Cookie> cookies, List<NameValuePair> parameters, List<NameValuePair> files) throws Exception
  {
    PostMethod method = new PostMethod(url);
    HttpState state = new HttpState();

    state.setCookiePolicy(CookiePolicy.COMPATIBILITY);
    client.setState(state);

    if(headers != null && headers.size() > 0)
    {
      for(Header header : headers)
        method.addRequestHeader(header);
    }

    state.clearCookies();
    if(cookies != null && cookies.size() > 0)
    {
      for(Cookie cookie : cookies)
        state.addCookie(cookie);
    }

    if(files != null && files.size() > 0)
    {
      ArrayList<Part> partList = new ArrayList<Part>();

      for(NameValuePair pair : files)
      {
        File file = new File(pair.getValue());
        partList.add(new FilePart(pair.getName(), file));
      }

      if(parameters != null && parameters.size() > 0)
      {
        for(NameValuePair pair : parameters)
          partList.add(new StringPart(pair.getName(), pair.getValue()));
      }

      Part[] parts =  new Part[partList.size()];
      parts = partList.toArray(parts);
      method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
    }
    else if(parameters != null && parameters.size() > 0)
    {
      for(NameValuePair pair : parameters)
        method.addParameter(pair.getName(), pair.getValue());
    }

    client.executeMethod(method);

    this.responseCode = method.getStatusCode();
    this.headers = method.getResponseHeaders();
    this.cookies = state.getCookies();
    this.content = method.getResponseBodyAsString();

    method.releaseConnection();
  }

  public int getResponseCode()
  {
    return this.responseCode;
  }

  public Header[] getResponseHeaders()
  {
    return this.headers;
  }

  public Cookie[] getCookies()
  {
    return this.cookies;
  }

  public String getResponseContentAsString()
  {
    return this.content;
  }

  public void logonToYahoo(String uid, String password) throws Exception
  {
    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new NameValuePair("login", uid));
    parameters.add(new NameValuePair("passwd", password));
    parameters.add(new NameValuePair(".save", "Sign In"));

    doPostConnect("https://login.yahoo.com/config/login/", null, null, parameters, null);
  }

  public void logonToBouncer(String id, String password) throws Exception
  {
    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new NameValuePair("id", id));
    parameters.add(new NameValuePair("pass_word", password));
    parameters.add(new NameValuePair("action", "login"));

    doPostConnect("https://bouncer.by.corp.yahoo.com/login/", null, null, parameters, null);
  }
}
