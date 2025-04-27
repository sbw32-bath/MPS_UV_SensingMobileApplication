package com.example.uvmonitor.database;

public class UVSummary {

    public String date;
    public int maxUv;
    public int minUv;

    public UVSummary(String date, int maxUv, int minUv) {
        this.date = date;
        this.maxUv = maxUv;
        this.minUv = minUv;
    }
}
