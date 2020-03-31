package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to create a branch when the local branch
 * already exists
 */
public class LocalBranchExistsException extends JGitFlowException
{
    public LocalBranchExistsException()
    {
    }

    public LocalBranchExistsException(String message)
    {
        super(message);
    }

    public LocalBranchExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LocalBranchExistsException(Throwable cause)
    {
        super(cause);
    }
}
