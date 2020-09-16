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

public class ContactSearch extends CommonTest{

    @DataProvider(name = "ContactNicknameSearch")
    public Object[][] contactNicknameDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw1")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "cs")
         .addParameter("p", "Scott");

        return new Object[][]
        {
          { "770236", y }
        };
    }

    @DataProvider(name = "ContactNameSearch")
    public Object[][] contactNameDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw1")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("p", "Utopia");

        return new Object[][]
        {
          { "771695", y }
        };
    }


    /* 770236 */
    @Test(groups = {"smoke"},dataProvider = "ContactNicknameSearch")
    public void searchContactNicknameTest(String TMdata, YahooHTTPTestAPI y) throws Exception{
        String expGuid = "Z7VNTLYKHKFL5B7MQQXFLDLNJE";
        String guidPath = "/data/profiles/list/guid";

        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][searchContactTest]]]]"+ repn.getResponseContentAsString());

        y.verifyXPathValue(guidPath,expGuid);

    }


    /* 771695 */
    @Test(groups = {"smoke"}, dataProvider = "ContactNameSearch")
    public void searchContactNameTest(String TMdata, YahooHTTPTestAPI y) throws Exception{
        String guidPath = "/data/profiles/list/guid";

        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][searchContactNameTest]]]]"+ repn.getResponseContentAsString());

        String friendsGuid[] = {
            "Z7VNTLYKHKFL5B7MQQXFLDLNJE", //utopia_be_tw4
            "EX3TVDM2QUM2CHNGAXWHXTERRU", //utopia_be_tw5
            "JGR74JRZDMHVMK32ZQEPVE4WR4", //utopia_be_tw6
            "VOVLU4PSCD6XRALM45274ZR6WY", //utopia_be_tw7
            "H45GTQSLAMMN2WLAIK4G7QMDWM", //utopia_be_tw8
            "DBC6FOHOGHDBMFV6PDUUMVZQXY"  //utopia_be_tw9
        };

        y.verifyXPathValuesContains(guidPath,friendsGuid);

    }
}
