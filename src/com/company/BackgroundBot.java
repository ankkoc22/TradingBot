package com.company;

import com.sun.jndi.toolkit.url.Uri;
import jdk.nashorn.internal.parser.JSONParser;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

public class BackgroundBot {

    private String BASE_URL = "https://bittrex.com/api/1.1";
    private String RESULT = "result";
    private String ACCESS_SPECIFIER = "public";
    private String KEY_BASE_CURRENCY = "BaseCurrency";
    private String KEY_MARKET_CURRENCY = "MarketCurrency";
    private String KEY_MARKET_NAME = "MarketName";
    private String KEY_ASK = "Ask";
    private String KEY_BID = "Bid";
    public MarketAskName highestEth = new MarketAskName();


    public void getAnswer() throws Exception {
        URL relativeURL = new URL("https://bittrex.com/api/v1.1/public/getmarkets   ");

        HttpURLConnection urlConnection = (HttpURLConnection) relativeURL.openConnection();

        urlConnection.setRequestMethod("GET");
        int responseCode = urlConnection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream())
        );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray jsonArray = jsonObject.getJSONArray(RESULT);

        ArrayList<String> btc = new ArrayList<>();
        ArrayList<String> eth = new ArrayList<>();
        urlConnection.disconnect();

        URL MarketSummaryRelativeURL = new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries ");

        HttpURLConnection MSUrlConnection = (HttpURLConnection) MarketSummaryRelativeURL.openConnection();


        responseCode = MSUrlConnection.getResponseCode();

        in = new BufferedReader(
                new InputStreamReader(MSUrlConnection.getInputStream())
        );

        response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        JSONObject MSObject = new JSONObject(response.toString());
        JSONArray MSArray = MSObject.getJSONArray(RESULT);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);
            String baseCurrency = temp.getString(KEY_BASE_CURRENCY);
            String marketCurrecy = temp.getString(KEY_MARKET_CURRENCY);
            if (baseCurrency.equals("BTC")) {
                btc.add(marketCurrecy);
            } else if (baseCurrency.equals("ETH")) {
                eth.add(marketCurrecy);
            }
        }

        ArrayList<MarketAskName> marketAskNames = new ArrayList<>();
        Iterator<String> btcIterator = btc.iterator();

        while (btcIterator.hasNext()) {
            String btcName = btcIterator.next();
            Iterator<String> ethIterator = eth.iterator();
            while (ethIterator.hasNext()) {
                String etcName = ethIterator.next();
                if (btcName.equals(etcName)) {
                    // get ask value
                    MarketAskName tempMAN = new MarketAskName();
                    for (int i = 0; i < MSArray.length(); i++) {
                        JSONObject temp = MSArray.getJSONObject(i);
                        String marketName = temp.getString(KEY_MARKET_NAME);
                        String ask = temp.getString(KEY_ASK);
                        String bid = temp.getString(KEY_BID);
                        if (marketName.equals("BTC-" + btcName)) {
                            tempMAN.setMarketName("BTC-" + btcName);
                            tempMAN.setAsk(ask);
                        }
                        if (marketName.equals("ETH-" + etcName)) {
                            tempMAN.setBid(bid);
                        }
                    }
                    marketAskNames.add(tempMAN);
                    break;
                }
            }
        }
        double max = Double.MIN_VALUE;

        MSUrlConnection.disconnect();
        Iterator<MarketAskName> iterator = marketAskNames.iterator();
        while (iterator.hasNext()) {
            MarketAskName temp = iterator.next();
            double res = Double.parseDouble(temp.getBid()) * temp.getCoins();
            if (max < res) {
                max = Double.parseDouble(temp.getBid()) * temp.getCoins();
                highestEth = temp;
            }

        }
        URL ETHrelativeURL = new URL("https://bittrex.com/api/v1.1/public/getmarketsummary?market=btc-eth");

        urlConnection = (HttpURLConnection) ETHrelativeURL.openConnection();

        urlConnection.setRequestMethod("GET");
        responseCode = urlConnection.getResponseCode();

        response = new StringBuffer();
        in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream())
        );

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject jsonObject1 = new JSONObject(response.toString());
        JSONArray jsonArray1 = jsonObject1.getJSONArray(RESULT);
        JSONObject temp1 = jsonArray1.getJSONObject(0);
        String bid = temp1.getString(KEY_BID);

        System.out.println(Double.parseDouble(bid) * (Double.parseDouble(highestEth.getBid()) * highestEth.getCoins()));
        urlConnection.disconnect();

        MongoClient mongoClient = new MongoClient("localhost",27017);
        DB database = mongoClient.getDB("TradingBot");
        DBCollection collecction = database.getCollection("Arbitraj");

        DBObject person = new BasicDBObject("_id",MarketAskName.cycle );
        MarketAskName.cycle++;
            person.put("Market", new BasicDBObject("MarketName",highestEth.getMarketName())
                                                    .append("Ask",Double.parseDouble(highestEth.getAsk()))
                                                        .append("Coins",highestEth.getCoins())
                                                        .append("Bid",Double.parseDouble(highestEth.getBid()))
                                                        .append("Rate of Coins",(Double.parseDouble(highestEth.getBid()) * highestEth.getCoins()))
                                                        .append("New BTC",Double.parseDouble(bid) * (Double.parseDouble(highestEth.getBid()) * highestEth.getCoins())));

        collecction.insert(person);


    }
}
