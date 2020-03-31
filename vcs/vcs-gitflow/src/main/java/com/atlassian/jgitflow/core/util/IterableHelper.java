package com.atlassian.jgitflow.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for common Iterable operations
 */
public class IterableHelper
{
    /**
     * Returns a {List} of the contents for the given {Iterable}
     *
     * @param iterable The Iterable
     * @param <T>      The type of objects contained in the {Iterable}
     * @return A {List} of the iterable's objects
     */
    public static <T> List<T> asList(Iterable<T> iterable)
    {
        List<T> list = new ArrayList<T>();

        for (T item : iterable)
        {
            list.add(item);
        }

        return list;
    }
}
