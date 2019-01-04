import java.util.Random;

public class ArrayTransformTest {

	public static void main(String[] args) {
		
		
		final int rows = 1000000;
		final int column = 100;
		int[][] rawArray = new int[rows][column];
		
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < column; j++) {
				Random rand = new Random();
				
				int oneRandNum = rand.nextInt(100);
				rawArray[i][oneRandNum] = 1;
			}
		}

		//printArray(rawArray);
		
		long start = System.currentTimeMillis();
		
		int[][] transArray = transformArray(rawArray);
		
		long end = System.currentTimeMillis();
		long run = end - start;
		
		//printArray(transArray);
		
		System.out.println("\nrun time: " + run + " ms");

	}

	private static int[][] transformArray(int[][] rawArray) {
		int[][] result = new int[rawArray[0].length][rawArray.length];
		
		for(int i = 0; i < rawArray[0].length; i++) {
			for(int j = 0; j < rawArray.length; j++) {
				result[i][j] = rawArray[j][i];
			}
		}
		
		return result;
	}

	private static void printArray(int[][] array) {
		System.out.println("\n\n***************************\n");
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[0].length; j++) {
				System.out.printf("%3d", array[i][j]);
			}
			System.out.println();
		}
		
	}

}
