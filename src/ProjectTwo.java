
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectTwo {
    static final int ASCII_SHIFT = 48;
    static final int NUM_RANGE = 99;
    static byte[][] rawNumArray;

    // static ArrayList<ArrayList<Integer>> rawNumArrayList;

    static int support;
    static String outputFilename = "./Output_Dense_25000_20000_result.txt";

    public static void main(String[] args) throws IOException {

        String srcFilename = "./Dense_25000_20000.txt";

        long startTime = System.currentTimeMillis();

        ArrayList<Integer> numFreqResult = getFreqNum(srcFilename);

        ArrayList<int[]> candidateFreqsetList = getAndValidatePairs(numFreqResult);

        // rawNumArrayList.stream().forEach(System.out::println);

        // candidateFreqsetList = genAndValidateMultiItems(2, candidateFreqsetList);

        for (int i = 3; i <= NUM_RANGE + 1; i++) {
            candidateFreqsetList = genAndValidateMultiItems(i, candidateFreqsetList);
            if (candidateFreqsetList.size() == 0) {
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        System.out.println("running time: " + runTime + " ms");

    }

    private static ArrayList<int[]> genAndValidateMultiItems(int setSize, ArrayList<int[]> candidateFreqsetList)
            throws IOException {
        ArrayList<int[]> result = new ArrayList<>();
        // ArrayList<Integer> countList = new ArrayList<>();

        int candListSize = candidateFreqsetList.size();

        // System.out.println("setSize: " + setSize + ", candidateListSize: " +
        // candListSize);

        for (int i = 0; i < candListSize - 1; i++) {
            for (int j = i + 1; j < candListSize; j++) {
                int[] candSetOne = candidateFreqsetList.get(i);
                int[] candSetTwo = candidateFreqsetList.get(j);
                if (compareSubsetsMatched(candSetOne, candSetTwo)) {
                    addNewCandSetToList(setSize - 1, candSetOne, candSetTwo, result);
                } else {
                    break;
                }
            }
        }

        result = validateFreqset(setSize, result);

        // printSets(setSize, result);

        return result;
    }

    private static void addNewCandSetToList(int setSize, int[] candSetOne, int[] candSetTwo, ArrayList<int[]> result) {
        int[] tempArray = Arrays.copyOf(candSetOne, setSize + 1);
        tempArray[setSize] = candSetTwo[setSize - 1];

        // System.out.println("tempArray: " + Arrays.toString(tempArray));

        result.add(tempArray);
    }

    private static boolean compareSubsetsMatched(int[] candSetOne, int[] candSetTwo) {
        boolean result = true;
        int setLength = candSetOne.length;

        if (candSetOne[setLength - 1] > candSetTwo[setLength - 1]) {
            result = false;
        } else {
            for (int i = setLength - 2; i >= 0; i--) {
                if (candSetOne[i] != candSetTwo[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    private static ArrayList<int[]> getAndValidatePairs(ArrayList<Integer> numFreqResult) throws IOException {
        final int pairSize = 2;

        long start = System.currentTimeMillis();

        int numLength = numFreqResult.size();
        ArrayList<int[]> candidatePairList = new ArrayList<int[]>();

        for (int i = 0; i < numLength - 1; i++) {
            for (int j = i + 1; j < numLength; j++) {
                int[] onePair = new int[pairSize];

                onePair[0] = numFreqResult.get(i);
                onePair[1] = numFreqResult.get(j);

                // System.out.println("one pair: " + onePair[0] + ", " + onePair[1]);

                candidatePairList.add(onePair);
            }
        }

        // System.out.println("candidate size: " + candidatePairList.size() + ", 0: " +
        // candidatePairList.get(0)[0]);

        // printSets(2, candidatePairList);

        ArrayList<int[]> result = validateFreqset(pairSize, candidatePairList);

        long end = System.currentTimeMillis();
        long runTime = end - start;

        System.out.println("get and validate pairs, time: " + runTime + " ms, total freq num: " + numFreqResult.size());

        return result;
    }

    private static ArrayList<int[]> validateFreqset(int setSize, ArrayList<int[]> candidatePairList)
            throws IOException {

        long start = System.currentTimeMillis();

        ArrayList<int[]> result = new ArrayList<int[]>();
        ArrayList<Integer> countList = new ArrayList<>();

        boolean isAllNumMatched;
        int curCount;

        for (int i = 0, candidateSize = candidatePairList.size(); i < candidateSize;
             i++) {
            int[] oneCandidateSet = candidatePairList.get(i);

            // System.out.println("get one cand set: " +  Arrays.toString(oneCandidateSet));

            curCount = 0;
            for (int j = 0, rawSize = rawNumArray.length; j < rawSize; j++) {
                isAllNumMatched = true;
                for (int k = 0, setLength = oneCandidateSet.length; k < setLength; k++) {
                    int curNum = oneCandidateSet[k];
                    if
                    (rawNumArray[j][curNum] != 1) {
                        isAllNumMatched = false;
                        break;
                    }
                }
                if
                (isAllNumMatched) {
                    curCount++;
                }
            }
            if (curCount >= support) {
                result.add(oneCandidateSet);
                countList.add(curCount);
            }
        }

        // printSets(setSize, result);

        long end = System.currentTimeMillis();
        long runTime = end - start;

        System.out.println("validate freq sets, time: " + runTime + " ms, total cand sets: " + candidatePairList.size());

        if (result.size() != 0) {
            writeToFile(setSize, result, countList);
        }

        return result;
    }

    private static void writeToFile(int setSize, ArrayList<int[]> result, ArrayList<Integer> countList)
            throws IOException {

        System.out.println("set size: " + setSize + ", list size: " + result.size());

        long start = System.currentTimeMillis();

        BufferedWriter bfw = new BufferedWriter(new FileWriter(outputFilename, true));
        for (int i = 0, length = result.size(); i < length; i++) {
            bfw.append(Arrays.toString(result.get(i))).append(": ").append(String.valueOf(countList.get(i)));
            bfw.newLine();
        }

        bfw.close();

        long end = System.currentTimeMillis();
        long runTime = end - start;

        System.out.println("write to file, time: " + runTime + " ms");
    }

    private static void printSets(int setSize, ArrayList<int[]> list) {
        System.out.println("\nset size: " + list.size());

        for (int i = 0, size = list.size(); i < size; i++) {
            for (int j = 0; j < setSize; j++) {

                System.out.printf("%3d ", list.get(i)[j]);

            }
            System.out.println();
        }
    }

    private static ArrayList<Integer> getFreqNum(String srcFilename) throws IOException {
        long start = System.currentTimeMillis();

        ArrayList<Integer> result = new ArrayList<Integer>();

        int[] freqArray = new int[NUM_RANGE + 1];

        RandomAccessFile aFile = new RandomAccessFile(srcFilename, "r");
        FileChannel fChannel = aFile.getChannel();
        MappedByteBuffer mapBuffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, aFile.length());

        System.out.println("file length: " + aFile.length());

        int curValue = 0, totalRows = 0;

        totalRows = getOneNumFromFile(curValue, mapBuffer);

        System.out.println("\ntotal row: " + totalRows);

        /*
         * initial number of ArrayList<Integer> for storing raw data
         */

        rawNumArray = new byte[totalRows][NUM_RANGE + 1];

        // rawNumArrayList = new ArrayList<>(totalRows);

        support = getOneNumFromFile(curValue, mapBuffer);

        System.out.println("support: " + support + "\n");

        int oneNum, lineCR = 13, comma = 44;

        for (int i = 0; i < totalRows; i++) {
            oneNum = 0;

            // ArrayList<Integer> oneLineList = new ArrayList<>();

            while (true) {
                curValue = mapBuffer.get();
                if (curValue == comma) {
                    break;
                }
            }

            while (true) {
                if (mapBuffer.hasRemaining()) {
                    curValue = mapBuffer.get();
                    if (curValue == lineCR) {
                        freqArray[oneNum]++;
                        rawNumArray[i][oneNum] = 1;
                        // oneLineList.add(oneNum);

                        // rawNumArrayList.add(oneLineList);

                        // System.out.println("adding one list: " + oneLineList);

                        // System.out.println("array[" + i + "][" + oneNum + "]:" +
                        // rawNumArray[i][oneNum]);

                        oneNum = 0;
                        break;
                    } else {
                        curValue -= 48;
                        if (curValue >= 0 && curValue <= 9) {
                            oneNum = shiftAndConcatNum(curValue, oneNum);
                        } else {
                            freqArray[oneNum]++;
                            rawNumArray[i][oneNum] = 1;
                            // oneLineList.add(oneNum);

                            // System.out.println("array[" + i + "][" + oneNum + "]:" +
                            // rawNumArray[i][oneNum]);

                            oneNum = 0;
                        }
                    }
                } else {
                    freqArray[oneNum]++;
                    rawNumArray[i][oneNum] = 1;
                    // oneLineList.add(oneNum);

                    // rawNumArrayList.add(oneLineList);

                    // System.out.println("adding one list: " + oneLineList);

                    // System.out.println("array[" + i + "][" + oneNum + "]:" +
                    // rawNumArray[i][oneNum]);

                    break;
                }
            }
        }

        fChannel.close();
        aFile.close();

        long end = System.currentTimeMillis();
        long runTime = end - start;

        System.out.println("get freq num, time: " + runTime + " ms");

        //start = System.currentTimeMillis();

        for (int i = 0, arrayLength = freqArray.length; i < arrayLength; i++) {
            if (freqArray[i] >= support) {
                result.add(i);
            }
        }

        //end = System.currentTimeMillis();
        //runTime = end - start;

        //System.out.println("add freq to list, time: " + runTime + " ms");

        return result;

    }

    private static int getOneNumFromFile(int curValue, MappedByteBuffer mapBuffer) {
        int result = 0;
        while (true) {
            curValue = mapBuffer.get() - ASCII_SHIFT;
            if (curValue >= 0 && curValue <= 9) {
                result = shiftAndConcatNum(curValue, result);
            } else {
                break;
            }
        }
        return result;
    }

    private static int shiftAndConcatNum(int curValue, int preValue) {
        int result = -1, partOne, partTwo;
        partOne = preValue << 3;
        partTwo = preValue << 1;

        result = partOne + partTwo + curValue;

        return result;
    }

}
