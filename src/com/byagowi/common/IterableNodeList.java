package com.byagowi.common;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterable NodeList.
 *
 * @author ebraminio
 */
public class IterableNodeList implements Iterable<Node> {
    private final NodeList nodeList;

    public IterableNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            private int index = 0;

            /** {@inheritDoc} */
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            /** {@inheritDoc} */
            public Node next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeList.item(index++);
            }

            /** {@inheritDoc} */
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}