package com.yg.ai.stock.kospidata;

public class KospiData {
    private String date ;
    private double indexValue ;
    private double totalEa ;
    private double totalVolume ;
    private double ant ;
    private double foreigner ;
    private double company ;
    private double investBank ;
    private double investTrust ;

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

    public double getAnt() {
        return ant;
    }

    public void setAnt(double ant) {
        this.ant = ant;
    }

    public double getForeigner() {
        return foreigner;
    }

    public void setForeigner(double foreigner) {
        this.foreigner = foreigner;
    }

    public double getCompany() {
        return company;
    }

    public void setCompany(double company) {
        this.company = company;
    }

    public double getInvestBank() {
        return investBank;
    }

    public void setInvestBank(double investBank) {
        this.investBank = investBank;
    }

    public double getInvestTrust() {
        return investTrust;
    }

    public void setInvestTrust(double investTrust) {
        this.investTrust = investTrust;
    }

    public double getPensionFund() {
        return pensionFund;
    }

    public void setPensionFund(double pensionFund) {
        this.pensionFund = pensionFund;
    }

    private double pensionFund ;

}
