package com.atlassian.jgitflow.core.exception;

public class JGitFlowExtensionException extends JGitFlowException
{

    public JGitFlowExtensionException()
    {
    }

    public JGitFlowExtensionException(String message)
    {
        super(message);
    }

    public JGitFlowExtensionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JGitFlowExtensionException(Throwable cause)
    {
        super(cause);
    }
}
