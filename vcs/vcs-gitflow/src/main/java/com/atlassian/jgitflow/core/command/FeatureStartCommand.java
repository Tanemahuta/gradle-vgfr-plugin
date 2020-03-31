package com.atlassian.jgitflow.core.command;

import java.io.IOException;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.FeatureStartExtension;
import com.atlassian.jgitflow.core.extension.impl.EmptyFeatureStartExtension;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

/**
 * Start a feature.
 * <p>
 * This will create a new branch using the feature prefix and feature name from the tip of develop
 * </p>
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Start a feature:
 * <p></p>
 * <pre>
 * flow.featureStart(&quot;feature&quot;).call();
 * </pre>
 * <p></p>
 * Perform a fetch of develop before branching
 * <p></p>
 * <pre>
 * flow.featureStart(&quot;feature&quot;).setFetch(true).call();
 * </pre>
 */
public class FeatureStartCommand extends AbstractBranchCreatingCommand<FeatureStartCommand, Ref>
{
    private static final String SHORT_NAME = "feature-start";
    private FeatureStartExtension extension;

    /**
     * Create a new feature start command instance.
     * <p></p>
     * An instance of this class is usually obtained by calling {@link com.atlassian.jgitflow.core.JGitFlow#featureStart(String)}
     *
     * @param branchName The name of the feature
     * @param git        The git instance to use
     * @param gfConfig   The GitFlowConfiguration to use
     */
    public FeatureStartCommand(String branchName, Git git, GitFlowConfiguration gfConfig)
    {
        super(branchName, git, gfConfig);
        this.extension = new EmptyFeatureStartExtension();
    }

    /**
     * @return A reference to the new feature branch
     * @throws com.atlassian.jgitflow.core.exception.NotInitializedException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.BranchOutOfDateException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    @Override
    public Ref call() throws NotInitializedException, JGitFlowGitAPIException, LocalBranchExistsException, BranchOutOfDateException, JGitFlowIOException, LocalBranchMissingException, RemoteBranchExistsException, JGitFlowExtensionException, TagExistsException
    {
        String prefixedBranchName = runBeforeAndGetPrefixedBranchName(extension.before(), JGitFlowConstants.PREFIXES.FEATURE);

        enforcer().requireGitFlowInitialized();
        enforcer().requireLocalBranchAbsent(prefixedBranchName);

        try
        {
            doFetchIfNeeded(extension);

            Ref newBranch = doCreateBranch(gfConfig.getDevelop(), prefixedBranchName, extension);

            doPushNewBranchIfNeeded(extension, prefixedBranchName);

            runExtensionCommands(extension.after());
            return newBranch;

        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
        finally
        {
            reporter.endCommand();
            reporter.flush();
        }
    }

    @Override
    protected String getCommandName()
    {
        return SHORT_NAME;
    }

    public FeatureStartCommand setExtension(FeatureStartExtension extension)
    {
        this.extension = extension;
        return this;
    }
}
