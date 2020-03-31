package com.atlassian.jgitflow.core.exception;

/**
 * Superclass of all exceptions thrown by the API classes in
 * {@code com.atlassian.jgitflow.core}
 */
public class JGitFlowException extends Exception
{
    public JGitFlowException()
    {
        super();
    }

    public JGitFlowException(String message)
    {
        super(message);
    }

    public JGitFlowException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JGitFlowException(Throwable cause)
    {
        super(cause);
    }

}
