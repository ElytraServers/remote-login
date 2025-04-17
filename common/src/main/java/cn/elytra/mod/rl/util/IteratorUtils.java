package cn.elytra.mod.rl.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorUtils {

    /**
     * Collect all the elements from a {@link Iterator}, and stops at the maxCount.
     *
     * @param iterator the iterator
     * @param maxCount the max count
     * @param <T>      the type of elements
     * @return the list of elements.
     */
    public static <T> List<T> collectFromIterator(Iterator<T> iterator, int maxCount) {
        List<T> list = new ArrayList<T>();
        while(iterator.hasNext() && list.size() < maxCount) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Collect all the elements from a {@link Iterator}.
     *
     * @param iterator the iterator
     * @param <T>      the type of elements
     * @return the list of elements.
     */
    public static <T> List<T> collectFromIterator(Iterator<T> iterator) {
        return collectFromIterator(iterator, Integer.MAX_VALUE);
    }

}
