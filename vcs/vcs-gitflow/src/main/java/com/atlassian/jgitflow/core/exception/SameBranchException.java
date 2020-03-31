package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when master and develop branches are configured with
 * the same name
 */
public class SameBranchException extends JGitFlowException
{
    public SameBranchException()
    {
    }

    public SameBranchException(String message)
    {
        super(message);
    }

    public SameBranchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SameBranchException(Throwable cause)
    {
        super(cause);
    }
}
