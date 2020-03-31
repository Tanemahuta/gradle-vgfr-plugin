package com.atlassian.jgitflow.core.util;

/**
 * @since version
 */
public class CleanStatus
{
    private final boolean untracked;
    private final boolean uncommitted;
    private final String message;

    public CleanStatus(boolean untracked, boolean uncommitted, String message)
    {
        this.untracked = untracked;
        this.uncommitted = uncommitted;
        this.message = message;
    }

    public boolean isClean()
    {
        return (!uncommitted && !untracked);
    }

    public boolean isNotClean()
    {
        return (uncommitted || untracked);
    }

    public boolean isUntracked()
    {
        return untracked;
    }

    public boolean isUncommitted()
    {
        return uncommitted;
    }

    public String getMessage()
    {
        return message;
    }
}
