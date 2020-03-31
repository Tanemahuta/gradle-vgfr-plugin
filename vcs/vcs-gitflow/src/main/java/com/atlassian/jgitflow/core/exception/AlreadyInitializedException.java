package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to initialize git flow on a folder that
 * has already been initialized
 */
public class AlreadyInitializedException extends JGitFlowException
{
    public AlreadyInitializedException()
    {
    }

    public AlreadyInitializedException(Throwable cause)
    {
        super(cause);
    }

    public AlreadyInitializedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AlreadyInitializedException(String message)
    {
        super(message);
    }
}
