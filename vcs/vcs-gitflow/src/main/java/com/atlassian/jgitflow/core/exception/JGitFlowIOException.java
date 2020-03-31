package com.atlassian.jgitflow.core.exception;

/**
 * Exception to wrap IOException
 */
public class JGitFlowIOException extends JGitFlowException
{
    public JGitFlowIOException()
    {
    }

    public JGitFlowIOException(String message)
    {
        super(message);
    }

    public JGitFlowIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JGitFlowIOException(Throwable cause)
    {
        super(cause);
    }
}
