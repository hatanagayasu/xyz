package com.yahoo.utopia.controllerTest.social;

import com.yahoo.utopia.controllerTest.common.*;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.testng.Assert;
import org.testng.annotations.*;

import com.yahoo.testnglib.TMReporter.TM;
import com.yahoo.yqatestng.framework.YQA;

import com.yahoo.yqatestng.framework.wsmanager.testapi.HTTPTestAPI;
import com.yahoo.yqatestng.framework.wsmanager.Response;
//import com.yahoo.yqatestng.framework.wsmanager.RestRequest;
import com.yahoo.yqatestng.framework.wsmanager.Request;

import org.apache.commons.httpclient.NameValuePair;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;

import java.io.StringReader;

public class PeopleSearchAdv extends CommonTest{

    @DataProvider(name = "SearchLocation")
    public Object[][] locationDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw2")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("lo", "Bangalore");

        return new Object[][]
        {
          { "770019", y }
        };
    }

    @DataProvider(name = "SearchNickname")
    public Object[][] nicknamedataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw2")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("nn", "david");

        return new Object[][]
        {
          { "770019", y }
        };
    }

    @DataProvider(name = "SearchEmail")
    public Object[][] emaildataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw2")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("em", "utopia_be_tw3@yahoo.com");

        return new Object[][]
        {
          { "770020", y }
        };
    }

    /* 770019 */
    @Test(groups = {"smoke"},dataProvider = "SearchLocation")
    public void searchUserLocationTest(String TMdata, YahooHTTPTestAPI y) throws Exception{
        String expLocation = y.getParameter("lo");
        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][searchUserLocationTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        String locationPath = "/data/profiles/list/location";
        List<Element> locationList = doc.selectNodes(locationPath);
        for(Element locationElement : locationList){
            String location = locationElement.getText();
            Assert.assertTrue(location.contains(expLocation), "Location didn't match the location I search");
        }

    }


    /* 770019 */
    @Test(groups = {"smoke"}, dataProvider = "SearchNickname")
    public void searchUserNicknameTest(String TMdata, YahooHTTPTestAPI y) throws Exception{
        String expNickname = y.getParameter("nn");
        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][searchUserNicknameTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        String nnPath = "/data/profiles/list/nickname";
        List<Element> nnList = doc.selectNodes(nnPath);
        for(Element nicknameNode : nnList){
            String nickname = nicknameNode.getText().toLowerCase();
            Assert.assertTrue(nickname.contains(expNickname), "Nickname didn't match the nickname searched");
        }

    }

    /* 770020 */
    @Test(groups = {"Functional"})
    public void searchUserNicknameAndLocationTest() throws Exception{
        String expNickname = "david";
        String expLocation = "California";

        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw2")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("nn", expNickname);

        Response repn = y.sendActionRequest();

        /* Search David, get who is in California */
        System.out.println("[[[[[[ Response ][searchUserNicknameAndLocationTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        String locPath = "/data/profiles/list/location";
        List<Element> locList = doc.selectNodes(locPath);
        List<String> expGuidList = new ArrayList<String>();

        for(Element locationNode : locList){
            String location = locationNode.getText();
            if(location.contains(expLocation)){
                String guid = doc.valueOf("/data/profiles/list/guid");
                expGuidList.add(guid);
            }
        }

        /* People Search by Nickname and Location */
        y.addParameter("nn", expNickname);
        y.addParameter("lo", expLocation);

        repn = y.sendActionRequest();
        respContent = repn.getResponseContentAsString();
        reader = new SAXReader();
        sr = new StringReader(respContent);
        doc = reader.read(sr);

        String guidPath = "/data/profiles/list/guid";
        List<Element> guidElement = doc.selectNodes(guidPath);
        List<String> repnGuid = new ArrayList<String>();
        for(Element guidElm : guidElement)
           repnGuid.add(guidElm.getText());
        for(String guid : expGuidList){
            Assert.assertTrue(repnGuid.contains(guid));
        }

    }

    /* 770020 */
    @Test(groups = {"Functional"},dataProvider = "SearchEmail")
    public void searchUserEmailTest(String TMdata, YahooHTTPTestAPI y) throws Exception{
        String expGuid = "ART6WMJ5Y7JSWJFDMFNZJZEHCM";
        Response repn = y.sendActionRequest();
        System.out.println("[[[[[[ Response ][searchUserEmailTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        String guidPath = "/data/profiles/list/guid";
        String guid = doc.valueOf(guidPath);
        Assert.assertEquals(guid, expGuid, "Guid didn't match");

    }

}
