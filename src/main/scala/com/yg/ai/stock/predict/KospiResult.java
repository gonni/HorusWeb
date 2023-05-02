package com.yg.ai.stock.predict;

import java.util.List;

public class KospiResult {
    private String resultId ;
    private List<Double> predicted;
    private List<Double> actual;

    public KospiResult(String resultId, List<Double> predicted, List<Double> actual) {
        this.resultId = resultId;
        this.predicted = predicted;
        this.actual = actual;
    }

    public List<Double> getActual() {
        return actual;
    }

    public void setActual(List<Double> actual) {
        this.actual = actual;
    }
    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public List<Double> getPredicted() {
        return predicted;
    }

    public void setPredicted(List<Double> predicted) {
        this.predicted = predicted;
    }

}
