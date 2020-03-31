package com.atlassian.jgitflow.core.exception;

/**
 * Exception to wrap generic Exception thrown by 3rd party APIs
 */
public class JGitFlowGenericException extends JGitFlowException
{
    public JGitFlowGenericException()
    {
    }

    public JGitFlowGenericException(String message)
    {
        super(message);
    }

    public JGitFlowGenericException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JGitFlowGenericException(Throwable cause)
    {
        super(cause);
    }
}
