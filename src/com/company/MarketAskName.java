package com.company;

public class MarketAskName {

    private String marketName;
    private String ask;
    private String bid;

    public MarketAskName(){}

    public MarketAskName(String marketName, String ask) {
        this.marketName = marketName;
        this.ask = ask;

    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getAsk() {
        return ask;
    }

    public Double getCoins() { return 1/Double.parseDouble(ask); }

    public void setAsk(String ask) {
        this.ask = ask;
    }
}
