package com.cepmuvakkit.times.posAlgo;

/**
 * this class encapsulates a number represented in DMS (Degrees, 
 * Minutes, Seconds) format and provides some utility methods
 */
public class Dms {
	
	private int degree;
	
	private int minute;
	
	private double second;
	
	/**
	 * 
	 * @return degree value of the angle
	 */
	public int getDegree() {
		return degree;
	}
	
	/**
	 * 
	 * @return minute value of the angle
	 */
	public int getMinute() {
		return minute;
	}
	
	/**
	 * 
	 * @return second value of the angle
	 */
	public double getSecond() {
		return second;
	}
	
	/**
	 * Construct a DMS object using degree, minute and second values
	 * @param degree
	 * @param minute
	 * @param second
	 */
	public Dms(int degree, int minute, double second) {
		this.degree = degree;
		this.minute = minute;
		this.second = second;
	}
	
	/**
	 * construct a DMS object from a decimal value
	 * @param decimal
	 */
	public Dms(double decimal) {
		double tempmin;
		double v;
		v = Math.floor(decimal);
		degree = (int) v;
		tempmin = (decimal - v) * 60.0;
		
		v = Math.floor(tempmin);
		minute = (int) v;
		second = (tempmin - v) * 60.0;
		
	}
	
	/**
	 * return decimal value of the DMS angle. 
	 * @param dir reference direction. If direction is <code>Direction.SOUTH</code>
	 * or <code>Direction.WEST</code> then the decimal value is multiplied by -1
	 * @return signed decimal value of the DMS angle
	 * @see Direction
	 */
	public double getDecimalValue() {
		double sum = degree + ((minute / 60.0) + (second / 3600.0));
		
		return sum;
	}
	
	public String toString() {
		return degree + "�" + minute + "'" + second + "''";
	}
	
}
