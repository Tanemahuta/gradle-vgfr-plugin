package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to create a branch when the remote branch
 * already exists
 */
public class RemoteBranchExistsException extends JGitFlowException
{
    public RemoteBranchExistsException()
    {
    }

    public RemoteBranchExistsException(String message)
    {
        super(message);
    }

    public RemoteBranchExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RemoteBranchExistsException(Throwable cause)
    {
        super(cause);
    }
}
