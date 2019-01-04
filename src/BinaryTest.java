import java.util.Random;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

public class BinaryTest {
	public static void main(String[] args) {
		final int ROWS = 3;

		byte[] bitMap = new byte[ROWS];
		Random random = new Random();

		long start = System.currentTimeMillis();

		for (int i = 0; i < ROWS; i++) {
			int numInRow = random.nextInt(5);
			
			System.out.println("numInRow: " + numInRow);
			
			for (int j = 0; j < numInRow; j++) {
				int oneNum = random.nextInt(8);

				System.out.println("oneNum: " + oneNum);

				byte partOne = bitMap[i];
				
				System.out.print("partOne: " + Integer.toBinaryString(partOne));
				
				byte partTwo = (byte) (1 << oneNum);
				partOne |= partTwo;
				bitMap[i] = partOne;

				System.out.println( ", parttwo: "
						+ Integer.toBinaryString(partTwo) + ", bitmap: " + Integer.toBinaryString(bitMap[i]));

			}
		}

		long end = System.currentTimeMillis();
		System.out.println("\nrun time: " + (end - start) + " ms");

	}

}
