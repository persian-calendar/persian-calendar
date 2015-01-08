// Originally published on stackoverflow by myself: http://stackoverflow.com/a/9627911/1414809
package com.byagowi.common;

import java.util.Iterator;

/**
 * Range `iterator` for a safe `for`. This class will create an iterator from
 * linear series of integer numbers. For example new Range(2, 5) will make a
 * series of numbers from 2 with count of 5. (2, 3, 4, 5, 6).
 *
 * @author ebraminio
 */
public class Range implements Iterable<Integer> {
    private final int min;
    private final int count;

    public Range(int min, int count) {
        this.min = min;
        this.count = count;
    }

    /**
     * {@inheritDoc}
     */
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