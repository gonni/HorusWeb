package com.yg.ai.stock.predict;

import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yg.ai.stock.model.RecurrentNets;
import com.yg.ai.stock.representation.PriceCategory;
import com.yg.ai.stock.representation.StockDataSetIterator;
import com.yg.ai.stock.utils.PlotUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PredictMain extends StockPricePrediction {
    private static final Logger log = LoggerFactory.getLogger(PredictMain.class);
//    private static int exampleLength = 22;

    public static void main(String ... v) throws IOException {
        System.out.println("Active .. ");
//        String file = "datafile/prices-split-adjusted.csv";
        String file = "datafile/kospi.csv";

        String symbol =  "KOSPI"; //"GOOG"; // stock name
        int batchSize = 64; // mini-batch size
        double splitRatio = 0.98; // 90% for training, 10% for testing
        int epochs = 3; // training epochs

        log.info("Create dataSet iterator...");
        PriceCategory category = PriceCategory.CLOSE; // CLOSE: predict close price
        StockDataSetIterator iterator =
                new StockDataSetIterator(file, symbol, batchSize, exampleLength, splitRatio, category);
        log.info("Load test dataset...");

        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();

        log.info("Build lstm networks...");
        MultiLayerNetwork net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

        File locationToSave = new File("model/KospiLSTM_98_".concat(String.valueOf(category)).concat(".zip"));

        log.info("Training...");
        net.fit(iterator, epochs);

        log.info("Saving model...");
        ModelSerializer.writeModel(net, locationToSave, true);

        log.info("Load model...");
//        net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        net = MultiLayerNetwork.load(locationToSave, true);

        log.info("Testing...");
        if (category.equals(PriceCategory.ALL)) {
            INDArray max = Nd4j.create(iterator.getMaxArray());
            INDArray min = Nd4j.create(iterator.getMinArray());
            predictAllCategories(net, test, max, min);
        } else {
            double max = iterator.getMaxNum(category);
            double min = iterator.getMinNum(category);
            // ...
//            predictPriceOneAhead(net, test, max, min, category);
            predictPriceMultipleAhead(net, test, max, min, category);
        }
        log.info("Done...");
    }

    static void predictPriceMultipleAhead (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData,
                                           double max, double min, PriceCategory category) {
        System.out.println("Conduct MultiStep Predictions ..");
        double[] predicts = new double[testData.size()];
        double[] actuals = new double[testData.size()];

        for (int i = 0; i < testData.size(); i++) {
            double a = (testData.get(i).getKey()).getDouble(exampleLength - 1);
            System.out.println("input ->" + a);
            INDArray output = net.rnnTimeStep(testData.get(i).getKey());
            System.out.println("output -> " + output.getDouble(exampleLength - 1));

            predicts[i] = output.getDouble(exampleLength - 1) * (max - min) + min;
            actuals[i] = testData.get(i).getValue().getDouble(0);

            System.out.println(a + "->" + predicts[i] + "/" + actuals[i]);
        }
        log.info("Print out Predictions and Actual Values...");
        log.info("Predict,Actual");
        for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "," + actuals[i]);
        log.info("Plot...");
        PlotUtil.plot(predicts, actuals, String.valueOf(category));

    }


}
