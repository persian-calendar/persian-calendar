/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
// Originally published on stackoverflow by myself :D http://stackoverflow.com/questions/2950386/is-there-anything-like-enumerable-rangex-y-in-java/9627911#9627911
package com.byagowi.common;

import java.util.Iterator;

/**
 * Range `iterator` for a safe `for`. This class will create an iterator from
 * linear series of integer numbers. For example new Range(2, 5) will make a
 * series of numbers from 2 with count of 5. (2, 3, 4, 5, 6).
 * 
 * @author ebraminio
 * 
 */
public class Range implements Iterable<Integer> {
	private int min;
	private int count;

	public Range(int min, int count) {
		this.min = min;
		this.count = count;
	}

	/** {@inheritDoc} */
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int cur = min;
			private int count = Range.this.count;

			/** {@inheritDoc} */
			public boolean hasNext() {
				return count != 0;
			}

			/** {@inheritDoc} */
			public Integer next() {
				count--;
				return cur++; // first return the cur, then increase it.
			}

			/** {@inheritDoc} */
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}