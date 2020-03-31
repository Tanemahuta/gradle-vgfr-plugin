package com.atlassian.jgitflow.core.report;

/**
 * @since version
 */
public class JGitFlowReportEntry
{
    private final String id;
    private final String entry;
    private final boolean debug;
    private final boolean error;

    public JGitFlowReportEntry(String id, String entry, boolean debug, boolean error)
    {
        this.id = id;
        this.entry = entry;
        this.debug = debug;
        this.error = error;
    }

    public String getId()
    {
        return id;
    }

    public String getEntry()
    {
        return entry;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public boolean isError()
    {
        return error;
    }

    @Override
    public String toString()
    {
        return id + ": " + getEntry();
    }
}
