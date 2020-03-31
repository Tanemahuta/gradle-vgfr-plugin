package com.atlassian.jgitflow.core.command;

import java.io.IOException;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.ReleaseStartExtension;
import com.atlassian.jgitflow.core.extension.impl.EmptyReleaseStartExtension;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

/**
 * Start a release.
 * <p>
 * This will create a new branch using the release prefix and release name from the tip of develop
 * </p>
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Start a feature:
 * <p></p>
 * <pre>
 * flow.releaseStart(&quot;1.0&quot;).call();
 * </pre>
 * <p></p>
 * Perform a fetch of develop before branching
 * <p></p>
 * <pre>
 * flow.releaseStart(&quot;1.0&quot;).setFetch(true).call();
 * </pre>
 */
public class ReleaseStartCommand extends AbstractBranchCreatingCommand<ReleaseStartCommand, Ref>
{
    private static final String SHORT_NAME = "release-start";
    private ReleaseStartExtension extension;

    /**
     * Create a new release start command instance.
     * <p></p>
     * An instance of this class is usually obtained by calling {@link com.atlassian.jgitflow.core.JGitFlow#releaseStart(String)}
     *
     * @param releaseName The name of the release
     * @param git         The git instance to use
     * @param gfConfig    The GitFlowConfiguration to use
     */
    public ReleaseStartCommand(String releaseName, Git git, GitFlowConfiguration gfConfig)
    {
        super(releaseName, git, gfConfig);
        this.extension = new EmptyReleaseStartExtension();

    }

    /**
     * @return A reference to the new release branch
     * @throws com.atlassian.jgitflow.core.exception.NotInitializedException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.ReleaseBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.TagExistsException
     * @throws com.atlassian.jgitflow.core.exception.BranchOutOfDateException
     */
    @Override
    public Ref call() throws NotInitializedException, JGitFlowGitAPIException, ReleaseBranchExistsException, DirtyWorkingTreeException, JGitFlowIOException, LocalBranchExistsException, TagExistsException, BranchOutOfDateException, LocalBranchMissingException, RemoteBranchExistsException, JGitFlowExtensionException
    {
        String prefixedBranchName = runBeforeAndGetPrefixedBranchName(extension.before(), JGitFlowConstants.PREFIXES.RELEASE);

        enforcer().requireGitFlowInitialized();
        enforcer().requireNoExistingReleaseBranches();
        enforcer().requireLocalBranchAbsent(prefixedBranchName);
        enforcer().requireCleanWorkingTree(isAllowUntracked());

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

    public ReleaseStartCommand setExtension(ReleaseStartExtension extension)
    {
        this.extension = extension;
        return this;
    }
}
