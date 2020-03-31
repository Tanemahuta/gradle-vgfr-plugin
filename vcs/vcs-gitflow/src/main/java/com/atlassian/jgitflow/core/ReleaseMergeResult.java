package com.atlassian.jgitflow.core;

import org.eclipse.jgit.api.MergeResult;

/**
 * @since version
 */
public class ReleaseMergeResult
{
    private final MergeResult masterResult;
    private final MergeResult developResult;

    public ReleaseMergeResult(MergeResult masterResult, MergeResult developResult)
    {
        this.masterResult = masterResult;
        this.developResult = developResult;
    }

    public MergeResult getMasterResult()
    {
        return masterResult;
    }

    public MergeResult getDevelopResult()
    {
        return developResult;
    }

    public boolean wasSuccessful()
    {
        return (!masterHasProblems() && !developHasProblems());
    }

    public boolean masterHasProblems()
    {
        return !masterResult.getMergeStatus().isSuccessful();
    }

    public boolean developHasProblems()
    {
        return !developResult.getMergeStatus().isSuccessful();
    }

}
