package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when there are merge conflicts that need to be resolved
 */
public class MergeConflictsNotResolvedException extends JGitFlowException
{

    public MergeConflictsNotResolvedException()
    {
    }

    public MergeConflictsNotResolvedException(String message)
    {
        super(message);
    }

    public MergeConflictsNotResolvedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MergeConflictsNotResolvedException(Throwable cause)
    {
        super(cause);
    }
}
