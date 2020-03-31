package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to perform an operation on a remote
 * branch that doesn't exist
 */
public class RemoteBranchMissingException extends JGitFlowException
{
    public RemoteBranchMissingException()
    {
    }

    public RemoteBranchMissingException(String message)
    {
        super(message);
    }

    public RemoteBranchMissingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RemoteBranchMissingException(Throwable cause)
    {
        super(cause);
    }
}
