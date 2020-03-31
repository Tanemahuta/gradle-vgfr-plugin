package com.atlassian.jgitflow.core.command;

import java.io.IOException;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.HotfixStartExtension;
import com.atlassian.jgitflow.core.extension.impl.EmptyHotfixStartExtension;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

/**
 * Start a hotfix.
 * <p>
 * This will create a new branch using the hotfix prefix and release name from the tip of develop
 * </p>
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Start a feature:
 * <p></p>
 * <pre>
 * flow.hotfixStart(&quot;1.0.1&quot;).call();
 * </pre>
 * <p></p>
 * Perform a fetch of develop before branching
 * <p></p>
 * <pre>
 * flow.hotfixStart(&quot;1.0.1&quot;).setFetch(true).call();
 * </pre>
 */
public class HotfixStartCommand extends AbstractBranchCreatingCommand<HotfixStartCommand, Ref>
{
    private static final String SHORT_NAME = "hotfix-start";
    private HotfixStartExtension extension;

    /**
     * Create a new hotfix start command instance.
     * <p></p>
     * An instance of this class is usually obtained by calling {@link com.atlassian.jgitflow.core.JGitFlow#hotfixStart(String)}
     *
     * @param hotfixName The name of the hotfix
     * @param git        The git instance to use
     * @param gfConfig   The GitFlowConfiguration to use
     */
    public HotfixStartCommand(String hotfixName, Git git, GitFlowConfiguration gfConfig)
    {
        super(hotfixName, git, gfConfig);
        this.extension = new EmptyHotfixStartExtension();

    }

    /**
     * @return A reference to the new hotfix branch
     * @throws com.atlassian.jgitflow.core.exception.NotInitializedException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.HotfixBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.TagExistsException
     * @throws com.atlassian.jgitflow.core.exception.BranchOutOfDateException
     */
    @Override
    public Ref call() throws NotInitializedException, JGitFlowGitAPIException, HotfixBranchExistsException, DirtyWorkingTreeException, JGitFlowIOException, LocalBranchExistsException, TagExistsException, BranchOutOfDateException, LocalBranchMissingException, RemoteBranchExistsException, JGitFlowExtensionException
    {
        String prefixedBranchName = runBeforeAndGetPrefixedBranchName(extension.before(), JGitFlowConstants.PREFIXES.HOTFIX);

        enforcer().requireGitFlowInitialized();
        enforcer().requireNoExistingHotfixBranches();
        enforcer().requireLocalBranchAbsent(prefixedBranchName);
        enforcer().requireCleanWorkingTree(isAllowUntracked());

        try
        {
            doFetchIfNeeded(extension);

            Ref newBranch = doCreateBranch(gfConfig.getMaster(), prefixedBranchName, extension);

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

    public HotfixStartCommand setExtension(HotfixStartExtension extension)
    {
        this.extension = extension;
        return this;
    }
}
