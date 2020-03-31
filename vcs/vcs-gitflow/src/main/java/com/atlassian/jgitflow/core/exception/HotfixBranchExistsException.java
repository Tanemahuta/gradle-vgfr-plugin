package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to start a hotfix when a hotfix branch
 * already exists.
 */
public class HotfixBranchExistsException extends JGitFlowException
{
    public HotfixBranchExistsException()
    {
    }

    public HotfixBranchExistsException(String message)
    {
        super(message);
    }

    public HotfixBranchExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HotfixBranchExistsException(Throwable cause)
    {
        super(cause);
    }
}
