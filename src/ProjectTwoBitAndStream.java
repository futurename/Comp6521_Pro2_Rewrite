
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class ProjectTwoBitAndStream {
	static final int ASCII_SHIFT = 48;
	static final int NUM_RANGE = 99;
	static final int BIT_BYTES = 16;
	static final int BIT_VALID_WIDTH = 7;
	static byte[][] rawNumArray;

	// static ArrayList<ArrayList<Integer>> rawNumArrayList;

	static int support;
	static String outputFilename = "./Output_Dense_25000_20000_result.txt";

	public static void main(String[] args) throws IOException {

		String srcFilename = "./Dense_25000_20000.txt";

		long startTime = System.currentTimeMillis();

		ArrayList<Integer> numFreqResult = getFreqNum(srcFilename);

		/*
		 * for (int i = 0; i < rawNumArray.length; i++) { System.out.printf("%-3d:", i);
		 * for (int j = 0; j < BIT_BYTES; j++) { System.out.printf("%s ",
		 * Integer.toBinaryString(rawNumArray[i][j])); } System.out.println(); }
		 */
		ArrayList<int[]> candidateFreqsetList = getAndValidatePairs(numFreqResult);

		// candidateFreqsetList = genAndValidateMultiItems(2, candidateFreqsetList);

		for (int i = 3; i <= NUM_RANGE + 1; i++) {

			System.out.println("*********************** set size: " + i + " ***********************  free mem: "
					+ Runtime.getRuntime().freeMemory() + "\n");

			candidateFreqsetList = genAndValidateMultiItems(i, candidateFreqsetList);
			if (candidateFreqsetList.size() == 0) {
				break;
			}
		}

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		System.out.println("\n>>>>>>total running time: " + runTime + " ms");

	}

	private static ArrayList<int[]> genAndValidateMultiItems(int setSize, ArrayList<int[]> candidateFreqsetList)
			throws IOException {
		ArrayList<int[]> result = new ArrayList<>();
		// ArrayList<Integer> countList = new ArrayList<>();

		int candListSize = candidateFreqsetList.size();

		// System.out.println("setSize: " + setSize + ", candidateListSize: " +
		// candListSize);

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

		System.out.println("gen cand list, set size: " + setSize + ", running time: " + run + " ms");

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

		// long start = System.currentTimeMillis();

		System.out.println("*********************** set size: " + 2 + " ***********************\n");

		int numLength = numFreqResult.size();
		ArrayList<int[]> candidatePairList = new ArrayList<int[]>();

		// candidatePairList = numFreqResult.parallelStream().collect(oneFreqNum -> )

		long start = System.currentTimeMillis();

		for (int i = 0; i < numLength - 1; i++) {
			for (int j = i + 1; j < numLength; j++) {
				int[] onePair = new int[pairSize];

				onePair[0] = numFreqResult.get(i);
				onePair[1] = numFreqResult.get(j);

				// System.out.println("one pair: " + onePair[0] + ", " + onePair[1]);

				candidatePairList.add(onePair);
			}
		}

		long end = System.currentTimeMillis();
		long runTime = end - start;

		System.out.println("gen cand list, set size: " + pairSize + ", runnint time: " + runTime + " ms");

		// System.out.println("candidate size: " + candidatePairList.size() + ", 0: " +
		// candidatePairList.get(0)[0]);

		// printSets(2, candidatePairList);

		ArrayList<int[]> result = validateFreqset(pairSize, candidatePairList);

		// long end = System.currentTimeMillis();
		// long runTime = end - start;

		// System.out.println("total get and validate pairs, time: " + runTime + " ms,
		// total freq num: " + numFreqResult.size());

		return result;
	}

	private static ArrayList<int[]> validateFreqset(int setSize, ArrayList<int[]> candidatePairList)
			throws IOException {

		long start = System.currentTimeMillis();

		ArrayList<int[]> result = new ArrayList<int[]>();
		ArrayList<Integer> countList = new ArrayList<>();

		///////////////////////////////////////////////////////////////////////

		for (int[] oneCandSet : candidatePairList) {
			int matchCount = 0;
			matchCount = (int) Arrays.stream(rawNumArray).parallel().mapToInt(oneRawRow -> {
				for (int oneCandNum : oneCandSet) {

					/*
					 * System.out.println("one cand set: " + Arrays.toString(oneCandSet) +
					 * ", onerawrow: " + Integer.toBinaryString(oneRawRow[0]) + " " +
					 * Integer.toBinaryString(oneRawRow[1]));
					 */
					if (!isNumInBitMap(oneCandNum, oneRawRow)) {
						return 0;
					}
				}

				// System.out.println("set size: " + setSize + ", validate result: " + ((counter
				// == setSize) ? true: false));

				// return (counter == setSize) ? 1 : 0;
				
				return 1;

			}).sum();

			/*
			 * System.out.println("set size: " + setSize + ", array: " +
			 * Arrays.toString(oneCandSet) + ", matchcount: " + matchCount);
			 */

			if (matchCount >= support) {
				result.add(oneCandSet);
				countList.add(matchCount);
			}
		}

		////////////////////////////////////////////////////////////////////////////

		// printSets(setSize, result);

		long end = System.currentTimeMillis();
		long runTime = end - start;

		System.out.println("validate freq sets, time: " + runTime + " ms, total cand sets: " + candidatePairList.size()
				+ ", validated sets: " + result.size());

		if (result.size() != 0) {
			writeToFile(setSize, result, countList);
		}

		// System.out.println(countList);

		return result;
	}

	private static boolean isNumInBitMap(int oneNum, byte[] oneRawRow) {
		int byteSeq, byteRemainder;
		byte curByteValue, byteValue;

		byteSeq = oneNum / BIT_VALID_WIDTH;
		byteRemainder = oneNum % BIT_VALID_WIDTH;
		curByteValue = oneRawRow[byteSeq];
		byteValue = (byte) (1 << byteRemainder);

		/*
		 * System.out.println("inside-> onerawrow: " +
		 * Integer.toBinaryString(oneRawRow[0]) + " " +
		 * Integer.toBinaryString(oneRawRow[1]) + " ,cand: " + oneNum + ", byteseq: " +
		 * byteSeq + ", remainder: " + byteRemainder + ", bytevalue: " +
		 * Integer.toBinaryString(byteValue) + ", curbytevalue: " +
		 * Integer.toBinaryString(curByteValue) + ", result: " + ((byteValue &
		 * curByteValue) != 0));
		 */

		return (byteValue & curByteValue) != 0;
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

		System.out.println("write to file, time: " + runTime + " ms\n");
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

		rawNumArray = new byte[totalRows][BIT_BYTES];

		// rawNumArrayList = new ArrayList<>(totalRows);

		support = getOneNumFromFile(curValue, mapBuffer);

		System.out.println("support: " + support + "\n");

		int oneNum, lineCR = 13, comma = 44;

		for (int i = 0; i < totalRows; i++) {
			oneNum = 0;

			ArrayList<Integer> curRawRowMarkList = new ArrayList<Integer>(NUM_RANGE + 1);

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
						curRawRowMarkList.add(oneNum);

						processBitMap(curRawRowMarkList, i);

						// rawNumArray[i][oneNum] = 1;

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
							curRawRowMarkList.add(oneNum);

							// rawNumArray[i][oneNum] = 1;

							// System.out.println("array[" + i + "][" + oneNum + "]:" +
							// rawNumArray[i][oneNum]);

							oneNum = 0;
						}
					}
				} else {
					freqArray[oneNum]++;
					curRawRowMarkList.add(oneNum);

					processBitMap(curRawRowMarkList, i);

					// rawNumArray[i][oneNum] = 1;

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

		for (int i = 0, arrayLength = freqArray.length; i < arrayLength; i++) {
			if (freqArray[i] >= support) {
				result.add(i);
			}
		}

		System.out.println("*********************** set size: " + 1 + " ***********************\n");
		System.out.println("get freq num, time: " + runTime + " ms, freq size: " + result.size() + "\n");

		return result;

	}

	private static void processBitMap(ArrayList<Integer> curRawRowMarkList, int rowNum) {
		for (int k = curRawRowMarkList.size() - 1; k >= 0; k--) {
			int byteSeq, byteRemainder = 0;
			byte curByteValue, sumByteValue;
			int onePos = curRawRowMarkList.get(k);

			byteSeq = onePos / BIT_VALID_WIDTH;
			byteRemainder = onePos % BIT_VALID_WIDTH;
			curByteValue = rawNumArray[rowNum][byteSeq];
			sumByteValue = (byte) ((1 << byteRemainder) | curByteValue);

			rawNumArray[rowNum][byteSeq] = sumByteValue;

			/*
			 * System.out.println("rowNum: " + rowNum + ",pos: " + onePos + ", byteseq: " +
			 * byteSeq + ", remainder: " + byteRemainder + ",sum: " +
			 * Integer.toBinaryString(sumByteValue));
			 */
		}

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
