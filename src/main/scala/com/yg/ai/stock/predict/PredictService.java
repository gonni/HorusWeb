package com.yg.ai.stock.predict;

import com.google.protobuf.Message;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import com.yg.ai.stock.kospidata.KospiIndexCategory;
import com.yg.ai.stock.kospidata.KospiPartDsIterator;
import com.yg.ai.stock.kospidata.PartKospiData;
import com.yg.ai.stock.model.RecurrentNets;
import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredictService {
    private static final Logger log = LoggerFactory.getLogger(PredictService.class);
    static int exampleLength = 22 ;
    private String file = "datafile/kospi_with_investor.csv";

    private int batchSize = 64; // mini-batch size
    private double splitRatio = 0.98; // 90% for training, 10% for testing
    private int epochs = 100; // training epochs

    private KospiIndexCategory category = KospiIndexCategory.ALL;
    private MultiLayerNetwork net = null ;
    private KospiPartDsIterator iterator = null ;
    private List<Pair<INDArray, INDArray>> testData = null ;

    private static PredictService instance ;

    private PredictService() {
        loadModel();
    }

    public synchronized static PredictService getInstance() {
        if(instance == null) {
            instance = new PredictService();
        }
        return instance;
    }

    private void loadModel() {
        String file = "datafile/kospi_with_investor.csv";

        int batchSize = 64; // mini-batch size
        double splitRatio = 0.98; // 90% for training, 10% for testing

        KospiIndexCategory category = KospiIndexCategory.ALL;

        iterator = new KospiPartDsIterator(file, batchSize, exampleLength, splitRatio, category);
        testData = iterator.getTestDataSet();

        log.info("Build lstm networks...");
        File modelFile = new File("model/Kospi3x3Ep100.mdl");
        try {
            net = MultiLayerNetwork.load(modelFile, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<INDArray, INDArray>> convertTestData(List<PartKospiData> stockDataList) {
        return iterator.generateTestDataSet(stockDataList);
    }

    public List<KospiResult> predict(List<PartKospiData> stockDataList, int predictDays) {
        List<Pair<INDArray, INDArray>> targetSampleData = this.convertTestData(stockDataList);

        INDArray max = Nd4j.create(iterator.getMaxArray());
        INDArray min = Nd4j.create(iterator.getMinArray());

        INDArray[] predicts = new INDArray[targetSampleData.size() + predictDays];
        INDArray[] actuals = new INDArray[targetSampleData.size() + predictDays];
        INDArray outputAll = null ;
        for (int i = 0; i < targetSampleData.size(); i++) {
//            System.out.println("Input ->" + testData.get(i).getKey()) ;

            outputAll = net.rnnTimeStep(targetSampleData.get(i).getKey());
//            System.out.println("Predict Output ->" + outputAll) ;
            INDArray output = outputAll.getRow(exampleLength - 1);

            predicts[i] = output.mul(max.sub(min)).add(min);
            actuals[i] = targetSampleData.get(i).getValue();
        }

        for(int i=0;i < predictDays; i++) {
            actuals[targetSampleData.size() + i] = actuals[targetSampleData.size() - 1] ;
            INDArray output = net.rnnTimeStep(outputAll).getRow(exampleLength - 1);
            predicts[targetSampleData.size() + i] = output.mul(max.sub(min)).add(min); ;
        }

        ArrayList<KospiResult> result = new ArrayList<>();
        for (int n = 0; n < 3; n++) {
            ArrayList<Double> lstPred = new ArrayList<>();
            ArrayList<Double> lstActu = new ArrayList<>();

            for (int i = 0; i < predicts.length; i++) {
                lstPred.add(predicts[i].getDouble(n));
                lstActu.add(actuals[i].getDouble(n));
            }

            String name = "IDX_" + n;
            result.add(new KospiResult(name, lstPred, lstActu));
        }

        return result;
    }

    public List<KospiResult> predict(int predictDays) {
        INDArray max = Nd4j.create(iterator.getMaxArray());
        INDArray min = Nd4j.create(iterator.getMinArray());

        INDArray[] predicts = new INDArray[testData.size() + predictDays];
        INDArray[] actuals = new INDArray[testData.size() + predictDays];
        INDArray outputAll = null ;
        for (int i = 0; i < testData.size(); i++) {
//            System.out.println("Input ->" + testData.get(i).getKey()) ;

            outputAll = net.rnnTimeStep(testData.get(i).getKey());
//            System.out.println("Predict Output ->" + outputAll) ;
            INDArray output = outputAll.getRow(exampleLength - 1);

            predicts[i] = output.mul(max.sub(min)).add(min);
            actuals[i] = testData.get(i).getValue();
        }

        for(int i=0;i < predictDays; i++) {
            actuals[testData.size() + i] = actuals[testData.size() - 1] ;
            INDArray output = net.rnnTimeStep(outputAll).getRow(exampleLength - 1);
            predicts[testData.size() + i] = output.mul(max.sub(min)).add(min); ;
        }

        ArrayList<KospiResult> result = new ArrayList<>();
        for (int n = 0; n < 3; n++) {
            ArrayList<Double> lstPred = new ArrayList<>();
            ArrayList<Double> lstActu = new ArrayList<>();

            for (int i = 0; i < predicts.length; i++) {
                lstPred.add(predicts[i].getDouble(n));
                lstActu.add(actuals[i].getDouble(n));
            }

            String name = "IDX_" + n;
            result.add(new KospiResult(name, lstPred, lstActu));
        }

        return result;
    }

    public static void main(String ... v) {
        PredictService test = new PredictService();
        System.out.println("---------------------------");
        test.predict(3).forEach(System.out::println);
    }
}
