import com.mongodb.client.MongoCollection;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

import twitter4j.*;

import com.google.gson.*;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;

public class tweetsCrawler {
    public static String LOGIN_URL = "https://twitter.com/login";
    public static String ACTION_URL = "https://twitter.com/sessions";

    public static List<String> USER_AGENT = new ArrayList<String>(Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.79 Safari/537.36 Maxthon/5.2.1.6000", //maxthon header. workable
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36", //chrome
            "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko", //IE
            "Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16", //opera
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A", //safari header
            "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/61.0", //firefox header workable
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41"//workable
    )
    );

    public static int user_agent_index = 0;

    public static Map<String, String> map_cookies = null;
    public static String SEARCH_URL   = "https://twitter.com/i/search/timeline?f=tweets&vertical=default&q=from%%3A%s%%20since%%3A%s%%20until%%3A%s&src=typd&include_available_features=1&include_entities=1&reset_error_state=false&max_position=%s";

    public static String TWEETS_URL = "https://twitter.com/i/profiles/show/%s/timeline/with_replies?max_position=%s&reset_error_state=false";
    public static String START_URL = "https://twitter.com/i/profiles/show/%s/timeline/with_replies?min_position=%s&reset_error_state=false";
    public static String COMMENT_URL = "https://twitter.com/i/%s/conversation/%s?max_position=%s";

    public static List<String> person = new ArrayList<String>();
    public static int nbr_tweets;
    public static int nbr_comments;
    public static int nbr_person;

    public static Date start_date = null;

    public static boolean useMongo = false;
    public static MongoClient mongoClient = null;
    public static MongoDatabase mongoDatabase = null;

    public static int REFRESH_RATE = 0;
    public static boolean crawlComments = true;

    public static Timer timer = null;
    public static Thread thread = null;

    public static JTextArea tweetTA = null;
    public static JTextArea commentTA = null;

    public static JProgressBar crawlPB = null;
    public static JProgressBar tweetPB = null;
    public static JProgressBar comPB = null;

    public tweetsCrawler(final boolean crawl_comments, final boolean use_Mongo, String host, int port,
                         final String fromTime, final String toTime, long timeInterval,
                         JToggleButton jStartBtn, JTextArea jTweetTA, JTextArea jCommentTA,
                         JProgressBar jPBCrawl, JProgressBar jPBTweet, JProgressBar jPBCom, final JLabel jLabelInfo)
    {
        crawlPB = jPBCrawl;
        tweetPB = jPBTweet;
        comPB = jPBCom;

        tweetTA = jTweetTA;
        commentTA = jCommentTA;

        crawlComments = crawl_comments;
        useMongo = use_Mongo;
        if(useMongo){
            mongoClient = new MongoClient( host , port );
            mongoDatabase = mongoClient.getDatabase("twitter");
        }

        /*
        File dirFile = new File("Twitter");
        if(!dirFile.exists())
            dirFile.mkdir();*/

        final String dirName = "Output_Excel";
        File dirFile = new File(dirName);
        if(!dirFile.exists())
            dirFile.mkdir();

        try{
            simulateLogin("ibasicCASIC","ibasic1001");
            getNameListFromFile();
        }
        catch(Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new   PrintWriter(sw,true));
            print(sw.toString());
            if(e.getClass().getName().equals("java.net.ConnectException")){
                UIManager.put("OptionPane.okButtonText", "OK");
                JOptionPane.showMessageDialog(null, "Connection Timeout!\nPlease Use Proper Proxy.", "CONNECTION TIMEOUT",JOptionPane.WARNING_MESSAGE);
            }
            if(jLabelInfo != null)
                jLabelInfo.setText("Connection Timeout");
            jStartBtn.setText("Start");
            jStartBtn.setSelected(false);
            return;
        }

        //MONITOR START HERE..

        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                // task to run goes here
                thread = currentThread();
                try{
                    Date currentTime = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                    String dateString = formatter.format(currentTime);

                    print("START TASK AT " + dateString);
                    //System.out.println("START TASK AT " + dateString);
                    excelHandle excel = new excelHandle(dirName + "/twitter-" + dateString+".xlsx");

                    formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    dateString = formatter.format(currentTime);
                    MongoCollection<org.bson.Document> colUser = null;

                    if(crawlPB != null){
                        crawlPB.setMinimum(0);
                        crawlPB.setMaximum(nbr_person);
                        crawlPB.setValue(0);
                        crawlPB.setStringPainted(true);
                    }

                    for(int i = 0; i < person.size(); i++){
                        String screenName = person.get(i);
                        if(jLabelInfo != null)
                            jLabelInfo.setText("Crawling " + screenName + "...");
                        if(useMongo){

                            if(mongoDatabase.getCollection(screenName) == null)
                                mongoDatabase.createCollection(screenName);
                            colUser = mongoDatabase.getCollection(screenName);
                        }
                        org.bson.Document docUser = new org.bson.Document();

                        File dirPeople = new File("Twitter/" + screenName);
                        if(!dirPeople.exists())
                            dirPeople.mkdir();

                        print("User ScreenName: " + screenName);
                        //System.out.println("User ScreenName: " + screenName);
                        if( (screenName = getMetaInfo(screenName,docUser)) == null){
                            print("error screen name!");
                            continue;
                        }

                        if(nbr_tweets > 0){
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                            Date today = new Date();
                            Calendar c = Calendar.getInstance();
                            c.setTime(today);
                            c.add(Calendar.DAY_OF_MONTH, 1);
                            if(fromTime == null || fromTime.equals("0"))
                                crawlByKeyWords(screenName, sdf.format(start_date), sdf.format(c.getTime()),docUser);
                            else
                                crawlByKeyWords(screenName, fromTime, toTime, docUser);
                        }
                        else {
                            crawlUserTweets(screenName,docUser);
                        }

                        docUser.append("updateTime",dateString);
                        excel.writeExcel(docUser);
                        if(use_Mongo)
                            colUser.insertOne(docUser);
                        if(crawlPB != null)
                            crawlPB.setValue(i+1);
                    }
                    if(jLabelInfo != null){
                        jLabelInfo.setText("Finished Crawling! Waiting for next Round");
                    }
                }
                catch (Exception e){
                    print("Get Error: " + e.getStackTrace());
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, timeInterval * 60000);
    }

    public static void print(String text){
        if(tweetTA == null)
            System.out.println(text);
        else
            tweetTA.append(text + "\n");
    }

    public static void printComments(String text){
        if(tweetTA == null)
            System.out.println(text);
        else
            commentTA.append(text + "\n");
    }

    public static void simulateLogin(String userName, String pwd) throws Exception{
        Connection con;
        Connection.Response res = null;
        for(int i = 0 ; i < 100; i++){
            try{
                con = Jsoup.connect(LOGIN_URL).timeout(0).userAgent(USER_AGENT.get(user_agent_index));
                res = con.execute();
                break;
            }
            catch (SSLHandshakeException ssl){
                ssl.printStackTrace();
                System.out.println("SSL Handshake Err");
            }
        }


        Document doc = Jsoup.parse(res.body());
        List <Element> eleList = doc.select("form");
        Map<String, String> datas = new HashMap<String, String >();

        // 获取cooking和表单属性
        // lets make data map containing all the parameters and its values found in the form
        for (Element e : eleList.get(2).getAllElements()) {
            // 设置用户名
            if (e.attr("name").equals("session[username_or_email]")) {
                e.attr("value", userName);
            }
            // 设置用户密码
            if (e.attr("name").equals("session[password]")) {
                e.attr("value", pwd);
            }
            // 排除空值表单属性
            if (e.attr("name").length() > 0) {
                datas.put(e.attr("name"), e.attr("value"));
            }
        }


        /*
         * 第二次请求，以post方式提交表单数据以及cookie信息
         */
        Connection con2 = Jsoup.connect(ACTION_URL).timeout(0).userAgent(USER_AGENT.get(user_agent_index));
        con2.header("Referer","https://twitter.com/login");
        // 设置cookie和post上面的map数据

        if(tweetTA != null)
            tweetTA.setText("DATA: " + datas);
        //print("DATA: " + datas);
        //System.out.println("DATA: " + datas);

        Connection.Response login = con2.ignoreContentType(true).followRedirects(true).method(Connection.Method.POST)
                .data(datas).cookies(res.cookies()).execute();
        // 打印，登陆成功后的信息
        // parse the document from response
        //System.out.println(login.body());

        // 登陆成功后的cookie信息，可以保存到本地，以后登陆时，只需一次登陆即可
        map_cookies = login.cookies();
    }

    public static String getMetaInfo(String screenName, org.bson.Document docUser) throws Exception{
        File info_file = null;
        FileWriter fw = null;

        Twitter twitter = new TwitterFactory().getInstance();
        User user = null;
        try{
            user = twitter.showUser(screenName);
        }
        catch(TwitterException te){
            print(te.getErrorMessage() + " Maybe it's '\\n'");
            //System.out.println(te.getErrorMessage() + " Maybe it's '\\n'");
            return null;
        }

        /*
        try{
            info_file = new File("Twitter/totalMetaInfo.txt");
            fw = new FileWriter(info_file);
        }
        catch (Exception e){
            print("error: " + e.getMessage());
            //System.out.println("error: " + e.getMessage());
        }*/

        String name = user.getName();;
        String createdAt = user.getCreatedAt().toString();
        start_date = user.getCreatedAt();
        long id = user.getId();
        String location = user.getLocation();
        String url = user.getURL();
        nbr_tweets = user.getStatusesCount();
        int nbr_following = user.getFriendsCount();
        int nbr_follower = user.getFollowersCount();
        int nbr_like = user.getFavouritesCount();
        int nbr_list = user.getListedCount();

        if(tweetPB != null){
            tweetPB.setMaximum(nbr_tweets);
            tweetPB.setMinimum(0);
            tweetPB.setValue(0);
            tweetPB.setStringPainted(true);
        }

        /*fw.write(screenName + "\t" + name + "\t" + id + "\t" + createdAt + "\t" +
                location + "\t" + url + "\t" + nbr_tweets + "\t" +
                nbr_following + "\t" + nbr_follower + "\t" +
                nbr_like + "\t" + nbr_list + "\r\n");


        fw.write(screenName + "\t" + name + "\t" + nbr_tweets + "\t" +
                nbr_following + "\t" + nbr_follower + "\t" +
                nbr_like + "\t" + nbr_list + "\r\n");
        */

        docUser.append("screenName",screenName);
        docUser.append("userName",name);
        docUser.append("nbr_following",nbr_following);
        docUser.append("nbr_follower",nbr_follower);
        docUser.append("nbr_tweets",nbr_tweets);
        docUser.append("nbr_like",nbr_like);
        docUser.append("nbr_list",nbr_list);

        /*
        System.out.println("Username: " + name);
        System.out.println("id: " + id);
        System.out.println("location: " + location);
        System.out.println("url: " + url);
        System.out.println("createdAt: " + createdAt);
        System.out.println("tweets: " + nbr_tweets);
        System.out.println("following: " + nbr_following);
        System.out.println("follwer: " + nbr_follower);
        System.out.println("like: " + nbr_like);
        System.out.println("list: " + nbr_list);
        System.out.println("");
        */

        //fw.close();
        return user.getScreenName();
    }

    public static void getNameListFromFile() throws Exception{
        try{
            String filename = "personToCrawl.txt";
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp;
            while( (tmp = reader.readLine()) != null){
                person.add(tmp);
            }
            nbr_person = person.size();
        }catch (Exception e){
            print("Error:"+ e.getClass().getName() + ": " + e.getMessage());
            //System.err.println( "Error:"+ e.getClass().getName() + ": " + e.getMessage() );
        }

    }

    public static void getComments(String screenName, String id, org.bson.Document docDB) throws Exception {
        //screenName = "mike_pence";
        //id = "806534456932794368";
        //url:https://twitter.com/i/ceciliatan/conversation/999710559552622593?max_position=

        ArrayList<org.bson.Document> arr = new ArrayList<org.bson.Document>();

        String min_position = "";
        Connection con;
        Connection.Response res = null;
        JsonParser jparser = new JsonParser();
        JsonObject jsonobj = new JsonObject();

        JsonArray jsonarr = new JsonArray();
        int cnt = 0;

        if(tweetTA == null)
            System.out.println("Start Crawl " + nbr_comments + " Comments");
        else
            commentTA.setText("Start Crawl " + nbr_comments + " Comments\n");

        //System.out.println("Start Crawl " + nbr_comments + " Comments");
        while (true) {
            String url = String.format(COMMENT_URL, screenName, id, min_position);
            printComments("URL IN COMMENTS: " + url);
            //System.out.println("URL IN COMMENTS: " + url);

            for (int i = 1; i < 100; i++) {
                try {
                    con = Jsoup.connect(url).timeout(20 * 1000);
                    con.header("Referer", url);
                    con.userAgent(USER_AGENT.get(user_agent_index));
                    res = con.cookies(map_cookies).ignoreContentType(true).execute();
                } catch (SocketTimeoutException se) {
                    se.printStackTrace();
                    printComments("TIMEOUT IN COMMENTS, RETRYING... " + i);
                    //System.out.println("TIMEOUT IN COMMENTS, RETRYING... " + i);
                } catch (javax.net.ssl.SSLException jse) {
                    printComments("SSL ERR, RETRYING... " + i);
                    //System.out.println("SSL ERR, RETRYING... " + i);
                }
                catch (HttpStatusException he) {
                    for (int j = 1; j < 10000; j++) {
                        try {
                            he.printStackTrace();
                            printComments("sleeping for too many requests IN COMMENTS");
                            //System.out.println("sleeping for too many requests IN COMMENTS");

                            sleep(30 * 1000);
                            user_agent_index++;
                            if (user_agent_index == 6)
                                user_agent_index = 0;
                            con = Jsoup.connect(url).timeout(20 * 1000);
                            con.userAgent(USER_AGENT.get(user_agent_index));
                            con.header("Referer", url);
                            res = con.cookies(map_cookies).ignoreContentType(true).execute();
                            break;
                        } catch (HttpStatusException hhe) {
                            printComments("still too many requests IN COMMENTS " + j);
                            //System.out.println("still too many requests IN COMMENTS " + j);
                        }

                    }

                }
                try {
                    jsonobj = jparser.parse(res.body()).getAsJsonObject().get("descendants").getAsJsonObject();
                    break;
                } catch (Throwable ste) {
                    ste.printStackTrace();
                    printComments("TIMEOUT IN PARSE, RETRYING... " + i);
                    //System.out.println("TIMEOUT IN PARSE, RETRYING... " + i);
                }
            }

            //System.out.println(res.body());
            Document doc = Jsoup.parse(jsonobj.get("items_html").getAsString());
            //System.out.println("GET ITEMS:");
            //System.out.println(doc);
            //System.exit(-1);
            String text = null;

            List<Element> eleList = doc.select("body > li");
            if (eleList.size() == 0){
                if(crawlComments){
                    docDB.append("comment",jsonarr.toString());
                }
                break;
            }

            for (Element e : eleList) {
                Element es;
                String commentUrl = null;
                String commentName = null;
                String timestamp = null;
                String comment = null;

                JsonObject jobj = new JsonObject();
                if ((es = e.selectFirst("ol > div.ThreadedConversation-tweet > li > div")) != null) {
                    commentUrl = "http://twitter.com/" + es.attr("data-permalink-path");
                    commentName = es.attr("data-screen-name");

                    Element ess = null;
                    if( (ess = es.selectFirst("div.content > div.js-tweet-text-container > p")) == null)
                        continue;
                    comment = ess.text();
                    ess = es.selectFirst("div.content > div.stream-item-header > small > a > ._timestamp");
                    timestamp = ess.attr("data-time-ms");

                    //fw.write(es.attr("data-tweet-id") + "\t");
                } else if ((es = e.selectFirst("ol > li > div")) != null) {
                    commentUrl = "http://twitter.com/" + es.attr("data-permalink-path");
                    commentName = es.attr("data-screen-name");
                    Element ess = null;
                    if((ess = es.selectFirst("div.content > div.js-tweet-text-container > p")) == null)
                        continue;

                    comment = ess.text();
                    ess = es.selectFirst("div.content >  div.stream-item-header > small > a > ._timestamp");
                    timestamp = ess.attr("data-time-ms");
                    //fw.write(es.attr("data-tweet-id") + "\t");
                } else {
                    printComments("SHOW MORE HERE!");
                    //System.out.println("SHOW MORE HERE!");
                    es = e.selectFirst("button");
                    min_position = es.attr("data-cursor");
                    continue;
                }

                cnt++;
                if(comPB != null)
                    comPB.setValue(cnt);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String commentTime = dateFormat.format(new Date(new Long(timestamp)));

                printComments(cnt + "/" + nbr_comments + " " + commentTime + " " + commentName + ": " + comment);
                //System.out.println(cnt + "/" + nbr_comments + " " + commentTime + " " + commentName + ": " + comment);

                jobj.addProperty("time",commentTime);
                jobj.addProperty("name",commentName);
                jobj.addProperty("comment",comment);
                jobj.addProperty("commentURL",commentUrl);
                jsonarr.add(jobj); //jsonarr store comment info

                org.bson.Document docComment = new org.bson.Document("time",commentTime)
                        .append("name",commentName)
                        .append("comment",comment)
                        .append("commentURL",commentUrl);
                arr.add(docComment);
            }
            if (!jsonobj.get("min_position").isJsonNull()) {
                min_position = jsonobj.get("min_position").getAsString();   //update min_position
            }
            else if(doc.selectFirst("body > li.ThreadedConversation-showMoreThreads") == null){
                printComments("Finished Crawling Comments");
                printComments("Total comments: " + cnt);
                //System.out.println("Finished Crawling Comments");
                //System.out.println("Total comments: " + cnt);
                if(crawlComments){
                    //System.out.println("ARR: " + arr);
                    //docDB.append("comment",arr);
                    docDB.append("comment",jsonarr.toString());
                }
                break;
            }
        }
    }

   
    public static void crawlByKeyWords(String keywords, String fromtime, String totime, org.bson.Document docUser) throws Exception{
        ArrayList<org.bson.Document> tweetArr = new ArrayList<org.bson.Document>();

        File tweets_file = null;
        FileWriter fw = null;

        /*
        try{
            tweets_file = new File("Twitter/" + keywords + "/tweets.txt");
            fw = new FileWriter(tweets_file, true);
            fw.write("time\ttweet\tis_retweet\tnbr_retweet\tnbr_like\tnbr_reply\r\n");
        }
        catch (Exception e){
            print("error: " + e.getMessage());
            //System.out.println("error: " + e.getMessage());
        }*/

        String min_position = "";
        Connection con;
        Connection.Response res = null;
        JsonParser jparser = new JsonParser();
        JsonObject jsonobj = new JsonObject();
        int cnt = 0;
        while (true) {
            String url = String.format(SEARCH_URL, keywords, fromtime, totime, min_position);
            //https://twitter.com/i/search/timeline?f=tweets&vertical=default&q=from%3Amike_pence%20since%3A2009-02-28%20until%3A2018-07-31%20include%3Aretweets&src=typd&include_available_features=1&include_entities=1&reset_error_state=false&max_position=TWEET-1276010606-1022889897705783298
            print("URL: " + url);
            //System.out.println(url);

            for(int i = 1; i < 100; i++)
            {
                try{
                    con = Jsoup.connect(url).timeout(20*1000);
                    con.header("Referer",url);
                    con.userAgent(USER_AGENT.get(user_agent_index));
                    res = con.cookies(map_cookies).ignoreContentType(true).execute();
                }
                catch (SocketTimeoutException se){
                    print("TIMEOUT, RETRYING... " + i);
                    //System.out.println("TIMEOUT, RETRYING... " + i);
                }catch (javax.net.ssl.SSLException jse){
                    print("SSL ERR, RETRYING... " + i);
                    //System.out.println("SSL ERR, RETRYING... " + i);
                }
                catch (HttpStatusException he) {
                    for(int j = 1; j < 10000; j ++){
                        try{
                            print("sleeping for too many requests");
                            //System.out.println("sleeping for too many requests");

                            sleep(20 * 1000);
                            user_agent_index ++;
                            if(user_agent_index == 6)
                                user_agent_index = 0;
                            con = Jsoup.connect(url).timeout(20*1000);
                            con.userAgent(USER_AGENT.get(user_agent_index));
                            con.header("Referer",url);
                            res = con.cookies(map_cookies).ignoreContentType(true).execute();
                            break;
                        }
                        catch (HttpStatusException hhe){
                            print("still too many requests " + j);
                            //System.out.println("still too many requests " + j);
                        }

                    }

                }
                try{
                    jsonobj = jparser.parse(res.body()).getAsJsonObject();
                    break;
                }
                catch (Throwable ste){
                    print("TIMEOUT IN PARSE, RETRYING... " + i);
                    //System.out.println("TIMEOUT IN PARSE, RETRYING... " + i);
                }
            }
            if(jsonobj.get("min_position").isJsonNull())     //updated for header blocked
            {
                 print("Finished crawling!");
                 //System.out.println("Finished crawling!");
                 break;
            }else if(jsonobj.get("new_latent_count").getAsInt() == 0)
            {
                print("header's blocked, change another header");
                //System.out.println("header's blocked, change another header");
                user_agent_index ++;
                if(user_agent_index >= 6)
                {
                    user_agent_index = 0;

                    print("all header's blocked!");
                    //System.out.println("all header's blocked!");

                    String tmp_pos = jsonobj.get("min_position").getAsString();
                    print("tmp_pos: " + tmp_pos);
                    //System.out.println("tmp_pos: " + tmp_pos);
                    if(tmp_pos.equals(min_position)){
                        print("Finished crawling!");
                        //System.out.println("Finished crawling!");
                        break;
                    }
                }
                min_position = jsonobj.get("min_position").getAsString();
                continue;
            }

            min_position = jsonobj.get("min_position").getAsString();   //update min_position

            Document doc = Jsoup.parse(jsonobj.get("items_html").getAsString());
            List<Element> eleList = doc.select("[class='js-stream-item stream-item stream-item']");
            if(eleList.size() == 0)
                break;
            for (Element e : eleList) {

                String id = null;
                String url_tweet = null;
                String time = null;
                String text = null;
                boolean is_retweet = false;
                String nbr_retweet = null;
                String nbr_like = null;
                String nbr_reply = null;
                boolean with_comment = true;

                Element es;
                if( (es = e.selectFirst("div")) != null) {
                    id = es.attr("data-tweet-id");
                    url_tweet = "http://twitter.com/" + keywords + "/status/" + id;
                    print(url_tweet);
                    //System.out.println(url_tweet);
                    //fw.write(es.attr("data-tweet-id") + "\t");
                }
                else continue;

                if( (es= e.selectFirst("div.js-tweet-text-container > p")) != null){
                    text = es.text();
                    //fw.write(text + "\t");
                }

                if( (es = e.selectFirst("div.stream-item-header > .time > a > span")) != null) {
                    long timestamp = Long.parseLong(es.attr("data-time-ms"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    time = dateFormat.format(new Date(timestamp));
                    //fw.write(time + "\t");
                    cnt ++;
                    if(tweetPB != null)
                        tweetPB.setValue(cnt);
                    print(cnt + "/" + nbr_tweets + " " + time + " @" + keywords + " : " + text);
                    //System.out.println(cnt + "/" + nbr_tweets + " " + time + " @" + keywords + " : " + text);

                }

                if( (es= e.selectFirst("div.content > div.QuoteTweet")) != null ) {     //with comment
                    //fw.write("true" + "\t");
                    is_retweet = true;
                    with_comment = true;
                }
                else if ((es= e.selectFirst("div.context > div > span.js-retweet-text")) != null){     //without comment
                    //fw.write("true" + "\t");
                    is_retweet = true;
                    with_comment = false;
                }
                else{
                    is_retweet = false;
                    //fw.write("false" + "\t");
                }

                if( (es = e.selectFirst("span.ProfileTweet-action--retweet > span.ProfileTweet-actionCount")) != null) {
                    nbr_retweet = es.attr("data-tweet-stat-count");
                    //fw.write( nbr_retweet + "\t");
                }

                if( (es = e.selectFirst("span.ProfileTweet-action--favorite > span.ProfileTweet-actionCount")) != null) {
                    nbr_like = es.attr("data-tweet-stat-count");
                    //fw.write(nbr_like + "\t");
                }

                if( (es = e.selectFirst("span.ProfileTweet-action--reply > span.ProfileTweet-actionCount")) != null) {
                    nbr_reply = es.attr("data-tweet-stat-count");
                    nbr_comments = Integer.parseInt(nbr_reply);

                    if(comPB != null){
                        comPB.setMinimum(0);
                        comPB.setMaximum(nbr_comments);
                        comPB.setValue(0);
                        comPB.setStringPainted(true);
                    }

                    //fw.write( nbr_reply + "\r\n");
                }

                org.bson.Document tweetDoc = new org.bson.Document();
                tweetDoc.append("id",id)
                        .append("time",time)
                        .append("tweet",text)
                        .append("url",url_tweet)
                        .append("is_retweet",is_retweet)
                        .append("nbr_retweet",nbr_retweet)
                        .append("nbr_like",nbr_like)
                        .append("nbr_reply",nbr_reply);
                if(crawlComments && (!(nbr_reply.equals("0") || (is_retweet && !with_comment))))
                    getComments(keywords,id,tweetDoc);
                tweetArr.add(tweetDoc);

                //fw.flush();
            }
            docUser.append("tweet",tweetArr);
        }
        //fw.close();
    }

    public static void stopTask(){
        thread.stop();
        timer.cancel();
    }

    public static void main(String[] args) throws Exception{
        System.getProperties().setProperty("https.proxyHost", "127.0.0.1");
        System.getProperties().setProperty("https.proxyPort", "1080");
        /*
        String url = "https://twitter.com/i/mike_pence/conversation/893239630400806912?max_position=";
        Connection con = Jsoup.connect(url).timeout(20 * 1000);
        con.userAgent(USER_AGENT.get(user_agent_index));
        Connection.Response res = con.ignoreContentType(true).execute();

        System.exit(-1);*/
        tweetsCrawler crawler = new tweetsCrawler(true, true, "localhost", 27017,
                null,null, 120,
                null, null,null ,
                null,null,null,null);
    }
}
