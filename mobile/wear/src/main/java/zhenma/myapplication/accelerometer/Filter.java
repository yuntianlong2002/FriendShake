/**
 * 
 */
package zhenma.myapplication.accelerometer;

/**
 * <p>This class provides the implementation for Butterworth and Exponential Smoothing Filter</p>
 * 
 * @author cs391mb
 * 
 */
public class Filter {

	public enum FilterType {BUTTERWORTH,SMOOTHING};
	
	private int SAMPLE_RATE = 30;
	
	private FilterType FILTER_TYPE = FilterType.SMOOTHING;
	
	private int SMOOTH_FACTOR = 3;
	
	private double CUTOFF_FREQUENCY = 1.0;
	
	private double ax[] = new double[3];
	private double by[] = new double[3];
	
	private double xv[][] = null;
	private double yv[][] = null;
	
	private double expectedValue[] =null;
	private static final double INVALID = Double.NEGATIVE_INFINITY;
	
	private static final int NUM_ACCEL_FIELDS = 3;
	private static final int X_INDEX = 0;
	private static final int Y_INDEX = 1;
	private static final int Z_INDEX = 2;

	
	/**
	 * Use this constructor if you want to use exponential smoothing filter
	 * @param smoothFactor
	 */
	public Filter(int smoothFactor) {
		FILTER_TYPE = FilterType.SMOOTHING;
		SMOOTH_FACTOR = (smoothFactor>=1?smoothFactor:1);
		expectedValue = new double[NUM_ACCEL_FIELDS];
	}
	
	/**
	 * Use this constructor if you want to use butterworth filter
	 * @param cutoffFrequency
	 */
	public Filter(double cutoffFrequency) {
		FILTER_TYPE = FilterType.SMOOTHING;
		CUTOFF_FREQUENCY = cutoffFrequency;
		xv = new double[NUM_ACCEL_FIELDS][3];
		yv = new double[NUM_ACCEL_FIELDS][3];
		getLPCoefficientsButterworth2Pole(SAMPLE_RATE, CUTOFF_FREQUENCY);
	}
	
	
	/**
	 * Get FilteredValues
	 * @param accX
	 * @param accY
	 * @param accZ
	 * @return
	 */
	public double[] getFilteredValues(double accX, double accY, double accZ) {
		double result[] = new double[NUM_ACCEL_FIELDS];
		if(FILTER_TYPE == FilterType.BUTTERWORTH) {
			result[X_INDEX] = getButterworthFilteredValue(accX, X_INDEX);
			result[Y_INDEX] = getButterworthFilteredValue(accY, Y_INDEX);
			result[Z_INDEX] = getButterworthFilteredValue(accZ, Z_INDEX);
		} 
		else if(FILTER_TYPE == FilterType.SMOOTHING) {
			result[X_INDEX] = getSmoothedValue(accX, X_INDEX);
			result[Y_INDEX] = getSmoothedValue(accY, Y_INDEX);
			result[Z_INDEX] = getSmoothedValue(accZ, Z_INDEX);
		}
		return result;
	}
	
	/**
	 * Filter using butterworth filter
	 * @param sample
	 * @param filterIndex
	 * @return
	 */
	private double getButterworthFilteredValue(double sample, int filterIndex) {
		xv[filterIndex][2] = xv[filterIndex][1]; xv[filterIndex][1] = xv[filterIndex][0];
		xv[filterIndex][0] = sample;
		yv[filterIndex][2] = yv[filterIndex][1]; yv[filterIndex][1] = yv[filterIndex][0];

		yv[filterIndex][0] =   (ax[0] * xv[filterIndex][0] + ax[1] * xv[filterIndex][1] + ax[2] * xv[filterIndex][2]
				- by[1] * yv[filterIndex][0]
						- by[2] * yv[filterIndex][1]);

		return yv[filterIndex][0];
	}
	
	/**
	 * Filter using Smoothing Filter
	 * @param sample
	 * @param filterIndex
	 * @return
	 */
	private double getSmoothedValue(double sample, int filterIndex) {
		if(expectedValue[filterIndex]==INVALID) {
			expectedValue[filterIndex] = sample;
			return expectedValue[filterIndex];
		}
		else {
			expectedValue[filterIndex] += (sample-expectedValue[filterIndex]) * SMOOTH_FACTOR/10;
			return expectedValue[filterIndex];
		}
	}
	
	/**
	 * Get Butterworth 2 Pole LPC Coefficients
	 * @param SAMPLE_RATE
	 * @param cutoff
	 */
	private void getLPCoefficientsButterworth2Pole(int SAMPLE_RATE, double cutoff)
	{
		double PI      = 3.1415926535897932385;
		double sqrt2 = 1.4142135623730950488;

		double QcRaw  = (2 * PI * cutoff) / SAMPLE_RATE; // Find cutoff frequency in [0..PI]
		double QcWarp = Math.tan(QcRaw); // Warp cutoff frequency

		double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));
		by[2] = (1 - sqrt2/QcWarp + 2/(QcWarp*QcWarp)) * gain;
		by[1] = (2 - 2 * 2/(QcWarp*QcWarp)) * gain;
		by[0] = 1;
		ax[0] = 1 * gain;
		ax[1] = 2 * gain;
		ax[2] = 1 * gain;
	}
	
	
	
	
	
}
