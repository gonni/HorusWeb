package com.yg.ai.stock.predict;

import com.yg.ai.stock.kospidata.KospiIndexCategory;
import com.yg.ai.stock.kospidata.KospiPartDsIterator;
import com.yg.ai.stock.model.RecurrentNets;
import javafx.util.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class KospiHybridPredictMain {
    private static final Logger log = LoggerFactory.getLogger(KospiHybridPredictMain.class);
    static int exampleLength = 22 ;

    public static void main(String ... v) throws IOException {
        System.out.println("Active ..");

        String file = "datafile/kospi_with_investor.csv";

        int batchSize = 64; // mini-batch size
        double splitRatio = 0.98; // 90% for training, 10% for testing
        int epochs = 100; // training epochs

        KospiIndexCategory category = KospiIndexCategory.ALL;

//        KospiHybridDsIterator iterator = new KospiHybridDsIterator(file, batchSize, exampleLength, splitRatio, category);
        KospiPartDsIterator iterator = new KospiPartDsIterator(file, batchSize, exampleLength, splitRatio, category);
        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();

        log.info("Build lstm networks...");
        MultiLayerNetwork net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

        File modelFile = new File("model/Kospi3x3Ep100.mdl");

//        log.info("Start Training ..");
//        net.fit(iterator, epochs);
//        log.info("Save Model ..");
//        ModelSerializer.writeModel(net, modelFile, true);

        net = MultiLayerNetwork.load(modelFile, true);

        log.info("Testing ..");
        INDArray max = Nd4j.create(iterator.getMaxArray());
        INDArray min = Nd4j.create(iterator.getMinArray());

//        predictAllCategories(net, test, max, min);    // original
        predictAllCategoriesMultiDays(net, test, max, min, 3);  // new predicted step by step
//        predictAllCategoriesMultiDaysV2(net, test, max, min, 3);  // added predicted src data

        System.out.println("Fin..");
    }

    private static INDArray nextStepMergedArray(INDArray targetData, INDArray predicted) {
        INDArray newInput = Nd4j.create(new int[] {exampleLength, 3}, 'f');
        System.out.println("InputLast Length => " + targetData.rows());

        for(int i=1; i<targetData.rows(); i++) {
//            System.out.println(i + " -> Element Shape -> " + targetData.getRow(i));

            for(int c=0;c<3;c++) {
                newInput.putScalar(new int[] {i-1, c}, targetData.getRow(i).getDouble(c));
                // --
            }
        }

        newInput.putScalar(new int[]{targetData.rows() -1, 0}, predicted.getDouble(0));
        newInput.putScalar(new int[]{targetData.rows() -1, 1}, predicted.getDouble(1));
        newInput.putScalar(new int[]{targetData.rows() -1, 2}, predicted.getDouble(2));

        return newInput;
    }

    static void predictAllCategoriesMultiDaysV2 (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData,
                                               INDArray max, INDArray min, int predictDays) {

        INDArray[] predicts = new INDArray[testData.size() + predictDays];
        INDArray[] actuals = new INDArray[testData.size() + predictDays];
        INDArray outputAll = null ;
        INDArray outputLast = null ;
        INDArray inputLast = null ;

        System.out.println("TestSize => " + testData.size());

        for (int i = 0; i < testData.size(); i++) {
            inputLast = testData.get(i).getKey() ;
            System.out.println("Input ->" + inputLast) ;

            outputAll = net.rnnTimeStep(inputLast);
            outputLast = outputAll.getRow(exampleLength - 1);
            System.out.println(i + "\tPredict Output ->" + outputLast) ;

            predicts[i] = outputLast.mul(max.sub(min)).add(min);
            actuals[i] = testData.get(i).getValue();
        }

        System.out.println("--------------- predict --------------->");
        // ----- predict future steps -----
        for(int i=testData.size(); i < testData.size() + predictDays; i++) {
            System.out.println(i + "\tinput N =>" + inputLast);
            inputLast = nextStepMergedArray(inputLast, outputLast);
            outputAll = net.rnnTimeStep(inputLast);
            outputLast = outputAll.getRow(exampleLength - 1);
            System.out.println(i + "\toutput N =>" + outputLast);

            predicts[i] = outputLast.mul(max.sub(min)).add(min);
            actuals[i] = actuals[testData.size() -1];
        }


//        INDArray newInput = nextStepMergedArray(inputLast, outputLast);
//        System.out.println("NewArray --> " + newInput);
        // ---------------------------

        log.info("Print out Predictions and Actual Values...");
        log.info("Predict\tActual");

        for (int i = 0; i < predicts.length ; i++) {
            log.info(i + "-->" + predicts[i] + "\t" + actuals[i]);
        }
        log.info("Plot...");

//        for (int n = 0; n < 3; n++) {
//            double[] pred = new double[predicts.length];
//            double[] actu = new double[actuals.length];
//            for (int i = 0; i < predicts.length ; i++) {
//                pred[i] = predicts[i].getDouble(n);
//                actu[i] = actuals[i].getDouble(n);
//            }
//            String name = "IDX_" + n ;
//
//            PlotUtil.plot(pred, actu, name);
//        }
    }

    /** Predict all the features (open, close, low, high prices and volume) of a stock one-day ahead */
    static void predictAllCategoriesMultiDays (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData,
                                               INDArray max, INDArray min, int predictDays) {
        INDArray[] predicts = new INDArray[testData.size() + predictDays];
        INDArray[] actuals = new INDArray[testData.size() + predictDays];
        INDArray outputAll = null ;
        for (int i = 0; i < testData.size(); i++) {
            System.out.println("Input ->" + testData.get(i).getKey()) ;

            outputAll = net.rnnTimeStep(testData.get(i).getKey());
            System.out.println("Predict Output ->" + outputAll) ;
            INDArray output = outputAll.getRow(exampleLength - 1);

            predicts[i] = output.mul(max.sub(min)).add(min);
            actuals[i] = testData.get(i).getValue();
        }

        for(int i=0;i < predictDays; i++) {
            actuals[testData.size() + i] = actuals[testData.size() - 1] ;

            INDArray output = net.rnnTimeStep(outputAll).getRow(exampleLength - 1);

            predicts[testData.size() + i] = output.mul(max.sub(min)).add(min); ;
        }

        log.info("Print out Predictions and Actual Values...");
        log.info("Predict\tActual");

        for (int i = 0; i < predicts.length ; i++) {
//            log.info(predicts.length + "/" + actuals.length);
            log.info(i + "-->" + predicts[i] + "\t" + actuals[i]);
        }
        log.info("Plot...");

//        for (int n = 0; n < 3; n++) {
//            double[] pred = new double[predicts.length ];
//            double[] actu = new double[actuals.length ];
//            for (int i = 0; i < predicts.length ; i++) {
//                pred[i] = predicts[i].getDouble(n);
//                actu[i] = actuals[i].getDouble(n);
//            }
//            String name = "IDX_" + n ;
//
//            PlotUtil.plot(pred, actu, name);
//        }
    }

    /** Predict all the features (open, close, low, high prices and volume) of a stock one-day ahead */
    static void predictAllCategories (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, INDArray max, INDArray min) {

        System.out.println("Max -> " + max);
        System.out.println("---------------------");
        System.out.println("Min -> " + min);


        INDArray[] predicts = new INDArray[testData.size()];
        INDArray[] actuals = new INDArray[testData.size()];
        for (int i = 0; i < testData.size(); i++) {
            System.out.println("Input ->" + testData.get(i).getKey()) ;
            INDArray output = net.rnnTimeStep(testData.get(i).getKey()).getRow(exampleLength - 1);
            System.out.println("Predict Output ->" + output) ;
            predicts[i] = output.mul(max.sub(min)).add(min);
//            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getRow(exampleLength - 1);
            actuals[i] = testData.get(i).getValue();
        }

        log.info("Print out Predictions and Actual Values...");
        log.info("Predict\tActual");
        for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "\t" + actuals[i]);
        log.info("Plot...");
//        for (int n = 0; n < 3; n++) {
//            double[] pred = new double[predicts.length];
//            double[] actu = new double[actuals.length];
//            for (int i = 0; i < predicts.length; i++) {
//                pred[i] = predicts[i].getDouble(n);
//                actu[i] = actuals[i].getDouble(n);
//            }
//            String name = "IDX_" + n ;
//
//            PlotUtil.plot(pred, actu, name);
//        }
    }
}
