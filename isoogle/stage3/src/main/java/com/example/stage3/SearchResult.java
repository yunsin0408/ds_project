package com.example.stage3;

/**
 * SearchResult
 * Container for saving API return data
 */
public class SearchResult {

    private String siteName;
    private String url;
    private int rankScore;
    private String content;

    public SearchResult(String siteName, String url) {
        this.siteName = siteName;
        this.url = url;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUrl() {
        return url;
    }

    public int getRankScore() {
        return rankScore;
    }

    public String getContent() {
        return content;
    }

    public void setRankScore(double rankScore) {
        this.rankScore = (int) rankScore;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
