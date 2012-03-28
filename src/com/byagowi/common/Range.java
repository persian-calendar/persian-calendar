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
// Originally published on stackoverflow be myself :D http://stackoverflow.com/questions/2950386/is-there-anything-like-enumerable-rangex-y-in-java/9627911#9627911
package com.byagowi.common;

import java.util.Iterator;

/**
 * Range iterator creator for safe iteration.
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