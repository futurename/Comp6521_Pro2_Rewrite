
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class ProjectTwo_P {
	static final int ASCII_SHIFT = 48;
	static final int NUM_RANGE = 99;
	static byte[][] rawNumArray;

	static int support;
	static String outputFilename = "./Output_Dense_25000_20000_result.txt";

	public static void main(String[] args) throws IOException {

		String srcFilename = "./Sparse_2500_50.txt";

		System.out.println("\nProcessing file: " + srcFilename.substring(2));

		long startTime = System.currentTimeMillis();

		ArrayList<Integer> numFreqResult = getFreqNum(srcFilename);

		ArrayList<int[]> candidateFreqsetList = getAndValidatePairs(numFreqResult);

		for (int i = 3; i <= NUM_RANGE + 1; i++) {
			candidateFreqsetList = genAndValidateMultiItems(i, candidateFreqsetList);
			if (candidateFreqsetList.size() == 0) {
				break;
			}
		}

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		System.out.println("\n>>>>>> running time: " + runTime + " ms");

	}

	private static ArrayList<int[]> genAndValidateMultiItems(int setSize, ArrayList<int[]> candidateFreqsetList)
			throws IOException {
		ArrayList<int[]> result = new ArrayList<>();

		int candListSize = candidateFreqsetList.size();

		long start = System.currentTimeMillis();

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

		long end = System.currentTimeMillis();
		long run = end - start;

		result = validateFreqset(setSize, result);

		return result;
	}

	private static void addNewCandSetToList(int setSize, int[] candSetOne, int[] candSetTwo, ArrayList<int[]> result) {
		int[] tempArray = Arrays.copyOf(candSetOne, setSize + 1);
		tempArray[setSize] = candSetTwo[setSize - 1];
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

		int numLength = numFreqResult.size();
		ArrayList<int[]> candidatePairList = new ArrayList<int[]>();

		long start = System.currentTimeMillis();

		for (int i = 0; i < numLength - 1; i++) {
			for (int j = i + 1; j < numLength; j++) {
				int[] onePair = new int[pairSize];

				onePair[0] = numFreqResult.get(i);
				onePair[1] = numFreqResult.get(j);
				candidatePairList.add(onePair);
			}
		}

		long end = System.currentTimeMillis();
		long runTime = end - start;

		ArrayList<int[]> result = validateFreqset(pairSize, candidatePairList);

		return result;
	}

	private static ArrayList<int[]> validateFreqset(int setSize, ArrayList<int[]> candidatePairList)
			throws IOException {

		long start = System.currentTimeMillis();

		ArrayList<int[]> result = new ArrayList<int[]>();
		ArrayList<Integer> countList = new ArrayList<>();

		for (int[] oneCandSet : candidatePairList) {
			int matchCount = 0;
			matchCount = (int) Arrays.stream(rawNumArray).parallel().mapToInt(oneRawRow -> {
				// int counter = 0;
				for (int oneCandNum : oneCandSet) {
					if (oneRawRow[oneCandNum] != 1) {
						return 0;
					}
				}
				return 1;
			}).sum();

			if (matchCount >= support) {
				result.add(oneCandSet);
				countList.add(matchCount);
			}
		}

		long end = System.currentTimeMillis();
		long runTime = end - start;

		if (result.size() != 0) {
			writeToFile(setSize, result, countList);
		}

		return result;
	}

	private static void writeToFile(int setSize, ArrayList<int[]> result, ArrayList<Integer> countList)
			throws IOException {
		BufferedWriter bfw = new BufferedWriter(new FileWriter(outputFilename, true));
		for (int i = 0, length = result.size(); i < length; i++) {
			bfw.append(Arrays.toString(result.get(i))).append(": ").append(String.valueOf(countList.get(i)));
			bfw.newLine();
		}
		bfw.close();
	}

	private static ArrayList<Integer> getFreqNum(String srcFilename) throws IOException {

		long start = System.currentTimeMillis();

		ArrayList<Integer> result = new ArrayList<Integer>();

		int[] freqArray = new int[NUM_RANGE + 1];

		RandomAccessFile aFile = new RandomAccessFile(srcFilename, "r");
		FileChannel fChannel = aFile.getChannel();
		MappedByteBuffer mapBuffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, aFile.length());

		int curValue = 0, totalRows = 0;

		totalRows = getOneNumFromFile(curValue, mapBuffer);

		System.out.println("\ntotal row: " + totalRows);

		rawNumArray = new byte[totalRows][NUM_RANGE + 1];

		support = getOneNumFromFile(curValue, mapBuffer);

		System.out.println("support: " + support + "\n");

		int oneNum, lineCR = 13, comma = 44;

		for (int i = 0; i < totalRows; i++) {
			oneNum = 0;

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
						oneNum = 0;
						break;
					} else {
						curValue -= 48;
						if (curValue >= 0 && curValue <= 9) {
							oneNum = shiftAndConcatNum(curValue, oneNum);
						} else {
							freqArray[oneNum]++;
							rawNumArray[i][oneNum] = 1;
							oneNum = 0;
						}
					}
				} else {
					freqArray[oneNum]++;
					rawNumArray[i][oneNum] = 1;
					break;
				}
			}
		}

		fChannel.close();
		aFile.close();

		long end = System.currentTimeMillis();
		long runTime = end - start;

		for (int i = 0, arrayLength = freqArray.length; i < arrayLength; i++) {
			if (freqArray[i] >= support) {
				result.add(i);
			}
		}

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
