package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to perform a git flow operation on
 * a folder that is not git flow initialized
 */
public class NotInitializedException extends JGitFlowException
{
    public NotInitializedException()
    {
    }

    public NotInitializedException(String message)
    {
        super(message);
    }

    public NotInitializedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotInitializedException(Throwable cause)
    {
        super(cause);
    }
}
