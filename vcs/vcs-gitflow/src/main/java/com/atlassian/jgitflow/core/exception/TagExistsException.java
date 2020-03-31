package com.atlassian.jgitflow.core.exception;

/**
 * Exception thrown when trying to create a new tag when a tag of
 * the same name already exists
 */
public class TagExistsException extends JGitFlowException
{
    public TagExistsException()
    {
    }

    public TagExistsException(String message)
    {
        super(message);
    }

    public TagExistsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TagExistsException(Throwable cause)
    {
        super(cause);
    }
}
