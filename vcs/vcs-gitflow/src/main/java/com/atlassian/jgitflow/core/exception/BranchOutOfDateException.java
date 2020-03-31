package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when a local branch is behind a remote branch
 */
public class BranchOutOfDateException extends JGitFlowException
{
    public BranchOutOfDateException()
    {
    }

    public BranchOutOfDateException(String message)
    {
        super(message);
    }

    public BranchOutOfDateException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BranchOutOfDateException(Throwable cause)
    {
        super(cause);
    }
}
