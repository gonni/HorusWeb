package com.yg.ai.stock.kospidata;

public class PartKospiData {
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(double indexValue) {
        this.indexValue = indexValue;
    }

    public double getTotalEa() {
        return totalEa;
    }

    public void setTotalEa(double totalEa) {
        this.totalEa = totalEa;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    private String date ;
    private double indexValue ;
    private double totalEa ;
    private double totalVolume ;

    public PartKospiData(String date, double indexValue, double totalEa, double totalVolume) {
        this.date = date;
        this.indexValue = indexValue;
        this.totalEa = totalEa;
        this.totalVolume = totalVolume;
    }

    @Override
    public String toString() {
        return this.date + "/" + this.indexValue + "/" + this.totalEa + "/" + this.totalVolume ;
    }

}