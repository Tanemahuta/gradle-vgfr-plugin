package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to start a release when a release
 * branch already exists.
 */
public class ReleaseBranchExistsException extends JGitFlowException
{
    public ReleaseBranchExistsException()
    {
    }

    public ReleaseBranchExistsException(String message)
    {
        super(message);
    }

    public ReleaseBranchExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ReleaseBranchExistsException(Throwable cause)
    {
        super(cause);
    }
}
