package com.yg.ai.stock.kospidata;

import com.opencsv.CSVReader;
import javafx.util.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class KospiPartDsIterator implements DataSetIterator {
    /** category and its index */
    private final Map<KospiIndexCategory, Integer> featureMapIndex = new HashMap<>();
    private final int VECTOR_SIZE = 3; // number of features for a stock data
    private int miniBatchSize; // mini-batch size
    private int exampleLength = 22; // default 22, say, 22 working days per month
    private int predictLength = 1; // default 1, say, one day ahead prediction

    /** minimal values of each feature in stock dataset */
    private double[] minArray = new double[VECTOR_SIZE];
    /** maximal values of each feature in stock dataset */
    private double[] maxArray = new double[VECTOR_SIZE];

    /** feature to be selected as a training target */
    private KospiIndexCategory category;

    /** mini-batch offset */
    private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

    /** stock dataset for training */
    private List<PartKospiData> train;
    /** adjusted stock dataset for testing */
    private List<Pair<INDArray, INDArray>> test;

    public KospiPartDsIterator(String filename, int miniBatchSize, int exampleLength,
                                 double splitRatio, KospiIndexCategory category) {
        featureMapIndex.put(KospiIndexCategory.INDEX_VALUE, 0);
        featureMapIndex.put(KospiIndexCategory.TOTAL_EA, 1);
        featureMapIndex.put(KospiIndexCategory.TOTAL_VOLUME, 2);

        List<PartKospiData> stockDataList = readStockDataFromFile(filename);
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        this.category = category;
        int split = (int) Math.round(stockDataList.size() * splitRatio);
        System.out.println("----- train:test split :" + split);
        train = stockDataList.subList(0, split);
        test = generateTestDataSet(stockDataList.subList(split, stockDataList.size()));
        initializeOffsets();
    }

    /** initialize the mini-batch offsets */
    private void initializeOffsets () {
        exampleStartOffsets.clear();
        int window = exampleLength + predictLength; // 22 + 1
        for (int i = 0; i < train.size() - window; i++) { exampleStartOffsets.add(i); }
    }

    public List<Pair<INDArray, INDArray>> getTestDataSet() { return test; }

    public double[] getMaxArray() { return maxArray; }

    public double[] getMinArray() { return minArray; }

    public double getMaxNum (KospiIndexCategory category) { return maxArray[featureMapIndex.get(category)]; }

    public double getMinNum (KospiIndexCategory category) { return minArray[featureMapIndex.get(category)]; }

//    private final int VECTOR_SIZE = 5; // number of features for a stock data
//    private int miniBatchSize; // mini-batch size
//    private int exampleLength = 22; // default 22, say, 22 working days per month
//    private int predictLength = 1; // default 1, say, one day ahead prediction

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();    // private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();
        int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());    // all - (22+1)

        INDArray input = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f'); // _,5,22
        INDArray label;

        if (category.equals(KospiIndexCategory.ALL)) label = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        else label = Nd4j.create(new int[] {actualMiniBatchSize, predictLength, exampleLength}, 'f'); // _,1,22

        for (int index = 0; index < actualMiniBatchSize; index++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
//            System.out.println("----------start=" + startIdx + ", endIdx=" + endIdx);
            PartKospiData curData = train.get(startIdx);    // get first - end block list  |start - [window] - end|

            PartKospiData nextData;
            for (int i = startIdx; i < endIdx; i++) {
                int c = i - startIdx;
                input.putScalar(new int[]{index, 0, c}, (curData.getIndexValue() - minArray[0]) / (maxArray[0] - minArray[0]));
                input.putScalar(new int[]{index, 1, c}, (curData.getTotalEa() - minArray[1]) / (maxArray[1] - minArray[1]));
                input.putScalar(new int[]{index, 2, c}, (curData.getTotalVolume() - minArray[2]) / (maxArray[2] - minArray[2]));

                nextData = train.get(i + 1);
                if (category.equals(KospiIndexCategory.ALL)) {
                    label.putScalar(new int[]{index, 0, c}, (nextData.getIndexValue() - minArray[0]) / (maxArray[0] - minArray[0]));
                    label.putScalar(new int[]{index, 1, c}, (nextData.getTotalEa() - minArray[1]) / (maxArray[1] - minArray[1]));
                    label.putScalar(new int[]{index, 2, c}, (nextData.getTotalVolume() - minArray[2]) / (maxArray[2] - minArray[2]));
                } else {
                    label.putScalar(new int[]{index, 0, c}, feedLabel(nextData));
                }
                curData = nextData;
            }
            if (exampleStartOffsets.size() == 0) break;
        }
        return new DataSet(input, label);
    }

    private double feedLabel(PartKospiData data) {
        double value;
        switch (category) {
            case INDEX_VALUE: value = (data.getIndexValue() - minArray[0]) / (maxArray[0] - minArray[0]); break;
            case TOTAL_EA: value = (data.getTotalEa() - minArray[1]) / (maxArray[1] - minArray[1]); break;
            case TOTAL_VOLUME: value = (data.getTotalVolume() - minArray[2]) / (maxArray[2] - minArray[2]); break;
            default: throw new NoSuchElementException();
        }
        return value;
    }

    public int totalExamples() { return train.size() - exampleLength - predictLength; }

    @Override public int inputColumns() { return VECTOR_SIZE; }

    @Override public int totalOutcomes() {
        if (this.category.equals(KospiIndexCategory.ALL)) return VECTOR_SIZE;
        else return predictLength;
    }

    @Override public boolean resetSupported() { return true; }

    @Override public boolean asyncSupported() { return false; }

    @Override public void reset() { initializeOffsets(); }

    @Override public int batch() { return miniBatchSize; }

    public int cursor() { return totalExamples() - exampleStartOffsets.size(); }

    public int numExamples() { return totalExamples(); }

    @Override public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override public DataSetPreProcessor getPreProcessor() { throw new UnsupportedOperationException("Not Implemented"); }

    @Override public List<String> getLabels() { throw new UnsupportedOperationException("Not Implemented"); }

    @Override public boolean hasNext() { return exampleStartOffsets.size() > 0; }

    @Override public DataSet next() { return next(miniBatchSize); }

    public List<Pair<INDArray, INDArray>> generateTestDataSet (List<PartKospiData> stockDataList) {
        int window = exampleLength + predictLength; // = 22 + 1
        List<Pair<INDArray, INDArray>> test = new ArrayList<>();
        for (int i = 0; i < stockDataList.size() - window; i++) {
            INDArray input = Nd4j.create(new int[] {exampleLength, VECTOR_SIZE}, 'f');  // 22, 5,vector_size = 5
            for (int j = i; j < i + exampleLength; j++) { // 22 + i, 0:j, j<22, j++ --> 1, j<23, j++, 2, j<24, ...)
                PartKospiData stock = stockDataList.get(j);
                input.putScalar(new int[] {j - i, 0}, (stock.getIndexValue() - minArray[0]) / (maxArray[0] - minArray[0]));
                input.putScalar(new int[] {j - i, 1}, (stock.getTotalEa() - minArray[1]) / (maxArray[1] - minArray[1]));
                input.putScalar(new int[] {j - i, 2}, (stock.getTotalVolume() - minArray[2]) / (maxArray[2] - minArray[2]));
            }

            PartKospiData stock = stockDataList.get(i + exampleLength);  // 22++
            INDArray label;
            if (category.equals(KospiIndexCategory.ALL)) {
                label = Nd4j.create(new int[]{VECTOR_SIZE}, 'f'); // ordering is set as 'f', faster construct
                label.putScalar(new int[]{0}, stock.getIndexValue());
                label.putScalar(new int[]{1}, stock.getTotalEa());
                label.putScalar(new int[]{2}, stock.getTotalVolume());

            } else {
                label = Nd4j.create(new int[] {1}, 'f');
                switch (category) {
                    case INDEX_VALUE: label.putScalar(new int[] {0}, stock.getIndexValue()); break;
                    case TOTAL_EA: label.putScalar(new int[] {0}, stock.getTotalEa()); break;
                    case TOTAL_VOLUME: label.putScalar(new int[] {0}, stock.getTotalVolume()); break;
                    default: throw new NoSuchElementException();
                }
            }
            test.add(new Pair<>(input, label));
        }
        return test;
    }

    private List<PartKospiData> readStockDataFromFile (String filename) {
        List<PartKospiData> stockDataList = new ArrayList<>();
        try {
            for (int i = 0; i < maxArray.length; i++) { // initialize max and min arrays
                maxArray[i] = Double.MIN_VALUE;
                minArray[i] = Double.MAX_VALUE;
            }
            List<String[]> list = new CSVReader(new FileReader(filename)).readAll(); // load all elements in a list
            for (String[] arr : list) {
//                if (!arr[1].equals(symbol)) continue;
                if (arr[0].contains("TARGET") || arr[1].equals("0") || arr[2].equals("0") || arr[3].equals("0")) continue;

                double[] nums = new double[VECTOR_SIZE];
                for (int i = 0; i < VECTOR_SIZE; i++) {
                    nums[i] = Double.valueOf(arr[i + 1]);
                    if (nums[i] > maxArray[i]) maxArray[i] = nums[i];
                    if (nums[i] < minArray[i]) minArray[i] = nums[i];
                }
                stockDataList.add(new PartKospiData(arr[0],nums[0],nums[1], nums[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0;i<maxArray.length;i++) {
            System.out.println("-----------------------");
            System.out.println(i + "--> max : min = " + maxArray[i] + " : " + minArray[i]);
        }

        return stockDataList;
    }
}


