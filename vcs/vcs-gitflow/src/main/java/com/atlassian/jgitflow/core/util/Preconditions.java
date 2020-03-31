package com.atlassian.jgitflow.core.util;

/**
 * A helper class to perform state checks
 */
public class Preconditions
{
    /**
     * Tests an expression to make sure it's true. If not, throws an {IllegalStateException}
     *
     * @param exp The expression to test
     */
    public static void checkState(boolean exp)
    {
        if (!exp)
        {
            throw new IllegalStateException();
        }
    }

    /**
     * Tests an object to ensure it's not null. If it is, throws an {IllegalStateException}
     *
     * @param obj The object to test
     */
    public static void checkNotNull(Object obj)
    {
        if (null == obj)
        {
            throw new IllegalStateException();
        }
    }
}
