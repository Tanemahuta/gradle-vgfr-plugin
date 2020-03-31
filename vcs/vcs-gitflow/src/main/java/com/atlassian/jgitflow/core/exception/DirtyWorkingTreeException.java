package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when the local working tree contains un-committed changes
 */
public class DirtyWorkingTreeException extends JGitFlowException
{

    public DirtyWorkingTreeException()
    {
    }

    public DirtyWorkingTreeException(String message)
    {
        super(message);
    }

    public DirtyWorkingTreeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DirtyWorkingTreeException(Throwable cause)
    {
        super(cause);
    }
}
