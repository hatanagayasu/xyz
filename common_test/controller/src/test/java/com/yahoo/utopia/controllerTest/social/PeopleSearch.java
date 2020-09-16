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

public class PeopleSearch extends CommonTest{

    @DataProvider(name = "UserHide")
    public Object[][] locationDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw2")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("p", "Utopia test");

	String be1_guid = "5QBKJQCPLWTBDO2WNSI357Y45M"

        return new Object[][]
        {
          { "770007", y , be1_guid}
        };
    }

    @DataProvider(name = "MutualFriend")
    public Object[][] MutualFriendDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw1")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("p", "Utopia test");

	String be2_guid = "YA7VYUB2KUAM2BP7OYHKD7RQXY";
        String be3_guid = "ART6WMJ5Y7JSWJFDMFNZJZEHCM";
        String be2_mfriend = "6";
        String be3_mfriend = "1";

        return new Object[][]
        {
          { "770012", y , be2_guid, be3_guid, be2_mfriend, be3_mfriend}
        };
    }

    @DataProvider(name = "NoResult")
    public Object[][] NoResultDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setUid("utopia_be_tw1")
         .setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("p", "Scotttttttttttttt");

	String total = "0";
	String count = "0";

        return new Object[][]
        {
          { "770016", y , total, count}
        };
    }

    @DataProvider(name = "NonLogin")
    public Object[][] NonLoginDataProvider()
    {
        YahooHTTPTestAPI y = new YahooHTTPTestAPI();

        y.setResponseCode("200")
         .setApplication("social")
         .setModule("search")
         .setAction("Index")
         .addAttribute("type", "ps")
         .addParameter("p", "Utopia test");

        return new Object[][]
        {
          { "770018", y }
        };
    }


    /* 77007 */
    @Test(groups = "smoke",dataProvider = "UserHide")
    public void peopleSearchUserHideTest(String TMdata, YahooHTTPTestAPI y, String be1_guid) throws Exception{
        //String be1_guid = "5QBKJQCPLWTBDO2WNSI357Y45M";

        Response repn = y.sendActionRequest();
        System.out.println("[[[[[ Response ][peopleSearchUserHideTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        Element ele = (Element)doc.selectNodes("/data/total").get(0);
        int total = Integer.valueOf((String)doc.valueOf("/data/total"));
        for(int i=0; i<total; i++){
            String xpath = "/data/profiles/A"+String.valueOf(i)+"/guid";
            String element = doc.valueOf(xpath);
            Assert.assertNotSame(element, be1_guid, "This guid is not searchable");
        }
    }

    /* 770012 */
    @Test(groups = {"Functional"},dataProvider = "MutualFriend")
    public void peopleSearchMutualFriendTest(String TMdata, YahooHTTPTestAPI y, String be2_guid, String be3_guid, String be2_mfriend, String be3_mfriend) throws Exception{
        //String be2_guid = "YA7VYUB2KUAM2BP7OYHKD7RQXY";
        //String be3_guid = "ART6WMJ5Y7JSWJFDMFNZJZEHCM";
        //String be2_mfriend = "6";
        //String be3_mfriend = "1";

        Response repn = y.sendActionRequest();
        System.out.println("[[[[[ Response ][peopleSearchMutualFriendTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        Element ele = (Element)doc.selectNodes("/data/total").get(0);
        int total = Integer.valueOf((String)doc.valueOf("/data/total"));
        int checkpoint = 0;
        for(int i=0; i<total; i++){
            String xpath = "/data/profiles/A"+String.valueOf(i);
            String guidpath = xpath+"/guid";
            String mfriendpath = xpath+"/friends_count";
            String friendsxpath = xpath+"/friends";
            String element = doc.valueOf(guidpath);
            if(element.equals(be2_guid)){
                Assert.assertNotSame(doc.valueOf(mfriendpath),be2_mfriend,"Num of Mutual friend is wrong");
                checkMutualFriend(doc, friendsxpath, doc.valueOf(mfriendpath));
                checkpoint++;
            }else if(element.equals(be3_guid)){
                Assert.assertNotSame(doc.valueOf(mfriendpath),be3_mfriend,"Num of Mutual friend is wrong");
                checkMutualFriend(doc, friendsxpath, doc.valueOf(mfriendpath));
                checkpoint++;
            }
        }
        Assert.assertNotSame(Integer.valueOf(checkpoint),"2","Didn't find all guid for mutual friend checking");
    }

    private void checkMutualFriend(Document doc, String path, String nMutualFriend){
        int nmfriend = Integer.valueOf(nMutualFriend);
        for(int i=0; i<nmfriend && i<5; i++){
            String friends = path + "/A" + String.valueOf(i);
            System.out.println(friends);
            Assert.assertNotNull(doc.selectSingleNode(friends),"Doesn't return all mutual friend");
        }
    }

    /* 770016 */
    @Test(groups = {"Functional"},dataProvider = "NoResult")
    public void peopleSearchNoResultTest(String TMdata, YahooHTTPTestAPI y, String total, String count) throws Exception{
        String keyword = y.getParameter("p");

        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][peopleSearchNoResultTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        String total = doc.valueOf("/data/total");
        String count = doc.valueOf("/data/count");
        String p = doc.valueOf("/data/p");

        Assert.assertEquals(total,total,"Return total result is not correct");
        Assert.assertEquals(count,count,"Return count result is not correct");
        Assert.assertEquals(p,keyword,"Return keyword result is not correct");
    }

    /* 770018 */
    @Test(groups = {"Functional"},dataProvider = "NonLogin")
    public void peopleSearchNonLoginTest(String TMdata, YahooHTTPTestAPI y) throws Exception{

        Response repn = y.sendActionRequest();
        System.out.println("[[[[ Response ][peopleSearchNonLoginTest]]]]"+ repn.getResponseContentAsString());

        String respContent = repn.getResponseContentAsString();
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(respContent);
        Document doc = reader.read(sr);

        int total = Integer.valueOf(doc.valueOf("/data/total"));
        String profilePath = "/data/profiles/";

        for(int i=0; i<total; i++){
            String mfPath = profilePath +"A"+ String.valueOf(i)+"/friends_count";
            String nMutualFriend = doc.valueOf(mfPath);
            Assert.assertEquals(nMutualFriend,"","Num of mutual friend should be 0.");
        }
    }
}
