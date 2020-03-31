package com.atlassian.jgitflow.core.exception;

/**
 * Exception to wrap GitAPIException thrown by JGit
 */
public class JGitFlowGitAPIException extends JGitFlowException
{
    public JGitFlowGitAPIException()
    {
    }

    public JGitFlowGitAPIException(String message)
    {
        super(message);
    }

    public JGitFlowGitAPIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JGitFlowGitAPIException(Throwable cause)
    {
        super(cause);
    }
}
