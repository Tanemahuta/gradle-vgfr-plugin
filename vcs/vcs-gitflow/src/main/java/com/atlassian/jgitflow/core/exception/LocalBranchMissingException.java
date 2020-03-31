package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to perform an operation on a local
 * branch that doesn't exist
 */
public class LocalBranchMissingException extends JGitFlowException
{
    public LocalBranchMissingException()
    {
    }

    public LocalBranchMissingException(String message)
    {
        super(message);
    }

    public LocalBranchMissingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LocalBranchMissingException(Throwable cause)
    {
        super(cause);
    }
}
