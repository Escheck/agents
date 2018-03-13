package parties.in4010.q12015.group10;

import static java.lang.Math.pow;
import Jama.Matrix;

public class boulwareParameterEstimator {

	static void printMatrix(Matrix myMat) {
		int n = myMat.getColumnDimension();
		int m = myMat.getRowDimension();
		System.out.println("------\nm=" + m + ",  n=" + n);
		// now loop through the rows of valsTransposed to print
		for (int row = 0; row < m; row++) {
			for (int col = 0; col < n; col++) {
				System.out.print(myMat.get(row, col) + ", ");
			}
			System.out.print("\n");
		}
	}

	public static double[] leastSquaresFit(double[] x, double[] time,
			int maxLoops, double powerIncrement) {

		// ////////////////////////////////////////////////////////////
		double[] a = new double[maxLoops];
		double[] b = new double[maxLoops];
		double[] c = new double[maxLoops];
		double[] d = new double[maxLoops];
		double[] varError = new double[maxLoops];
		double minimumVarError = 1000;
		int minimumVarErrorLoopNumber = 0;

		double powerT = 1; // initial boulware parameter

		for (int loopNumber = 0; loopNumber < maxLoops; loopNumber++) {
			// Perform least squares to find best fit for current boulware
			// parameter
			Matrix A = new Matrix(time.length, 3);
			Matrix B = new Matrix(3, 1);
			Matrix y = new Matrix(time.length, 1);
			powerT = powerT + powerIncrement;
			for (int n = 0; n < time.length; n++) {
				A.set(n, 0, 1);
				A.set(n, 1, time[n]);
				A.set(n, 2, pow(time[n], powerT));
				y.set(n, 0, x[n] - 1);
			}
			Matrix AT = A.transpose();
			Matrix ATA = AT.times(A);
			if (ATA.rank() < ATA.getColumnDimension()) {
				a[loopNumber] = 1;
				b[loopNumber] = 0;
				c[loopNumber] = 0;
				d[loopNumber] = 0;
			} else {
				try {
					Matrix invATA = ATA.inverse();
					Matrix invATAA = invATA.times(AT);
					B = invATAA.times(y);
				} catch (Exception e) {
					e.printStackTrace();
					a[loopNumber] = 1;
					b[loopNumber] = 0;
					c[loopNumber] = 0;
					d[loopNumber] = 0;
				}
			}

			a[loopNumber] = Math.min(B.get(0, 0), 1);
			b[loopNumber] = Math.min(B.get(1, 0), 0);
			c[loopNumber] = Math.min(B.get(2, 0), 0);
			d[loopNumber] = powerT;

			double[] xEst = new double[time.length];
			for (int n = 0; n < time.length; n++) {
				xEst[n] = a[loopNumber] + b[loopNumber] * time[n]
						+ c[loopNumber] * pow(time[n], d[loopNumber]);
			}
			varError[loopNumber] = MeanAndVariance.getVarianceError(x, xEst);
			if (minimumVarError > varError[loopNumber]) {
				minimumVarError = varError[loopNumber];
				minimumVarErrorLoopNumber = loopNumber;
			}
		}
		double[] parOUTabcE = new double[5];

		parOUTabcE[0] = a[minimumVarErrorLoopNumber];
		parOUTabcE[1] = b[minimumVarErrorLoopNumber];
		parOUTabcE[2] = c[minimumVarErrorLoopNumber];
		parOUTabcE[3] = d[minimumVarErrorLoopNumber];
		parOUTabcE[4] = minimumVarError;

		return parOUTabcE;
		// ////////////////////////////////////
	}

	static double[] getMinTime(double[] x, double[] time, int spacing,
			int beginOffset) {
		// Extract the minimum utility peaks
		int numberOfBlocks = time.length / spacing;
		if (numberOfBlocks * spacing > time.length) {
			numberOfBlocks = numberOfBlocks - 1;
		}

		double[] xMin = new double[numberOfBlocks - 1];
		double[] tMin = new double[numberOfBlocks - 1];
		int[] indexMin = new int[numberOfBlocks - 1];

		for (int blockNumber = 0; blockNumber < numberOfBlocks - 1; blockNumber++) {
			xMin[blockNumber] = 1;
			indexMin[blockNumber] = 0;
			for (int indexInBlock = 0; indexInBlock < spacing; indexInBlock++) {
				if (xMin[blockNumber] > x[blockNumber * spacing + indexInBlock]) {
					xMin[blockNumber] = x[blockNumber * spacing + indexInBlock];
					indexMin[blockNumber] = blockNumber * spacing
							+ indexInBlock;
					tMin[blockNumber] = time[indexMin[blockNumber]];
				}
			}

		}
		return tMin;
	}

	static double[] getMinEval(double[] x, double[] time, int spacing,
			int beginOffset) {
		// Extract the minimum utility peaks

		int numberOfBlocks = time.length / spacing;
		if (numberOfBlocks * spacing > time.length) {
			numberOfBlocks = numberOfBlocks - 1;
		}

		double[] xMin = new double[numberOfBlocks - 1];

		for (int blockNumber = 0; blockNumber < numberOfBlocks - 1; blockNumber++) {
			xMin[blockNumber] = 1;
			for (int indexInBlock = 0; indexInBlock < spacing; indexInBlock++) {
				if (xMin[blockNumber] > x[blockNumber * spacing + indexInBlock]) {
					xMin[blockNumber] = x[blockNumber * spacing + indexInBlock];
				}
			}

		}
		return xMin;
	}

	static int[] getMinIndex(double[] x, double[] time, int spacing,
			int beginOffset) {
		// Extract the minimum utility peaks

		int numberOfBlocks = time.length / spacing;
		if (numberOfBlocks * spacing > time.length) {
			numberOfBlocks = numberOfBlocks - 1;
		}

		double[] xMin = new double[numberOfBlocks - 1];
		int[] indexMin = new int[numberOfBlocks - 1];

		for (int blockNumber = 0; blockNumber < numberOfBlocks - 1; blockNumber++) {
			xMin[blockNumber] = 1;
			indexMin[blockNumber] = 0;
			for (int indexInBlock = 0; indexInBlock < spacing; indexInBlock++) {
				if (xMin[blockNumber] > x[blockNumber * spacing + indexInBlock]) {
					xMin[blockNumber] = x[blockNumber * spacing + indexInBlock];
					indexMin[blockNumber] = blockNumber * spacing
							+ indexInBlock;
				}
			}

		}
		return indexMin;
	}

}
