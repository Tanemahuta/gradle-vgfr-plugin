package com.atlassian.jgitflow.core.command;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Performs a rebase of the feature branch
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Rebase a feature:
 * <p></p>
 * <pre>
 * flow.featureRebase(&quot;feature&quot;).call();
 * </pre>
 */
public class FeatureRebaseCommand extends AbstractGitFlowCommand<FeatureRebaseCommand, Void>
{
    private static final String SHORT_NAME = "feature-rebase";

    /**
     * Create a new feature rebase command instance.
     * <p></p>
     * This command is usually run as part of a release finish by calling {@link FeatureFinishCommand#setRebase(boolean)}
     *
     * @param git      The git instance to use
     * @param gfConfig The GitFlowConfiguration to use
     */
    public FeatureRebaseCommand(String branchName, Git git, GitFlowConfiguration gfConfig)
    {
        super(branchName, git, gfConfig);
    }

    /**
     * @return nothing
     * @throws com.atlassian.jgitflow.core.exception.NotInitializedException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchMissingException
     */
    @Override
    public Void call() throws NotInitializedException, JGitFlowGitAPIException, DirtyWorkingTreeException, JGitFlowIOException, LocalBranchMissingException
    {
        String prefixedBranchName = gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.FEATURE.configKey()) + getBranchName();
        enforcer().requireGitFlowInitialized();
        enforcer().requireCleanWorkingTree(isAllowUntracked());
        enforcer().requireLocalBranchExists(prefixedBranchName);

        try
        {
            git.checkout().setName(prefixedBranchName).call();
            git.rebase().setUpstream(gfConfig.getDevelop()).call();
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }

        return null;
    }

    @Override
    protected String getCommandName()
    {
        return SHORT_NAME;
    }
}
