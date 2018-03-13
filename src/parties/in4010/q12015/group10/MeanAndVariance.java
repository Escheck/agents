package parties.in4010.q12015.group10;

public class MeanAndVariance {

	// Calculate the average error
	public static double getMeanError(double[] var1, double[] var2) {
		double meanError = 0;
		int arrayLength = var1.length;

		for (int arrayIndex = 0; arrayIndex < arrayLength; arrayIndex++) {
			meanError = meanError + var1[arrayIndex] - var2[arrayIndex];
		}
		meanError = meanError / ((double) arrayLength);
		return meanError;
	}

	public static double getVarianceError(double[] var1, double[] var2) {
		// by definition: VAR = E[X^2] - (E[X])^2
		double meanError = 0;
		double meanSquaredError = 0;
		double varianceError = 0;
		int arrayLength = var1.length;

		for (int arrayIndex = 0; arrayIndex < arrayLength; arrayIndex++) {
			meanError = meanError + var1[arrayIndex] - var2[arrayIndex];
			meanSquaredError = meanSquaredError
					+ (var1[arrayIndex] - var2[arrayIndex])
					* (var1[arrayIndex] - var2[arrayIndex]);
		}
		meanError = meanError / ((double) arrayLength);
		meanSquaredError = meanSquaredError / ((double) arrayLength);
		varianceError = meanSquaredError - meanError * meanError;
		return varianceError;
	}
}
