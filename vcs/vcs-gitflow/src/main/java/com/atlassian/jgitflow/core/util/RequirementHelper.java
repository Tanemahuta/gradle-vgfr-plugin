package com.atlassian.jgitflow.core.util;

import java.util.List;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.JGitFlowReporter;
import com.atlassian.jgitflow.core.exception.*;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class RequirementHelper
{
    protected final Git git;
    protected final GitFlowConfiguration gfConfig;
    protected final JGitFlowReporter reporter = JGitFlowReporter.get();
    protected final String commandName;

    public RequirementHelper(Git git, GitFlowConfiguration gfConfig, String commandName)
    {
        this.git = git;
        this.gfConfig = gfConfig;
        this.commandName = commandName;
    }

    /**
     * Requires that git flow has been initialized for the project represented by the internal {Git} instance
     *
     * @throws com.atlassian.jgitflow.core.exception.NotInitializedException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireGitFlowInitialized() throws NotInitializedException, JGitFlowGitAPIException
    {
        if (!gfConfig.gitFlowIsInitialized())
        {
            reporter.errorText(commandName, "requireGitFlowInitialized() failed");
            reporter.flush();
            throw new NotInitializedException("Git flow is not initialized in " + git.getRepository().getWorkTree().getPath());
        }
    }

    /**
     * Requires that a local branch with the given name does not yet exist
     *
     * @param branch the name of the branch to test
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireLocalBranchAbsent(String branch) throws LocalBranchExistsException, JGitFlowGitAPIException
    {
        if (GitHelper.localBranchExists(git, branch))
        {
            reporter.errorText(commandName, "requireLocalBranchAbsent() failed: '" + branch + "' already exists");
            reporter.flush();
            throw new LocalBranchExistsException("local branch '" + branch + "' already exists");
        }
    }

    /**
     * Requires that a local branch with the given name exists
     *
     * @param branch The name of the branch to test
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchMissingException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireLocalBranchExists(String branch) throws LocalBranchMissingException, JGitFlowGitAPIException
    {
        if (!GitHelper.localBranchExists(git, branch) && GitHelper.remoteBranchExists(git, branch))
        {
            try
            {
                git.checkout()
                   .setName(branch)
                   .setCreateBranch(true)
                   .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                   .setStartPoint(Constants.DEFAULT_REMOTE_NAME + "/" + branch)
                   .call();
            }
            catch (GitAPIException e)
            {
                throw new JGitFlowGitAPIException("error checking out remote branch.", e);
            }
        }

        if (!GitHelper.localBranchExists(git, branch))
        {
            reporter.errorText(commandName, "localBranchExists() failed: '" + branch + "' does not exist");
            reporter.flush();
            throw new LocalBranchMissingException("local branch " + branch + " does not exist");
        }
    }

    /**
     * Requires that a remote branch with the given name does not yet exist
     *
     * @param branch The name of the branch to test
     * @throws com.atlassian.jgitflow.core.exception.RemoteBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireRemoteBranchAbsent(String branch) throws RemoteBranchExistsException, JGitFlowGitAPIException
    {
        if (GitHelper.remoteBranchExists(git, branch))
        {
            reporter.errorText(commandName, "requireRemoteBranchAbsent() failed: '" + branch + "' already exists");
            reporter.flush();
            throw new RemoteBranchExistsException("remote branch '" + branch + "' already exists");
        }
    }

    /**
     * Requires that a remote branch with the given name exists
     *
     * @param branch The name of the branch to test
     * @throws com.atlassian.jgitflow.core.exception.RemoteBranchMissingException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireRemoteBranchExists(String branch) throws RemoteBranchMissingException, JGitFlowGitAPIException
    {
        if (!GitHelper.remoteBranchExists(git, branch))
        {
            reporter.errorText(commandName, "requireRemoteBranchExists() failed: '" + branch + "' does not exist");
            reporter.flush();
            throw new RemoteBranchMissingException("remote branch " + branch + " does not exist");
        }
    }

    /**
     * Requires that a tag with the given name does not yet exist
     *
     * @param name The name of the tag to test
     * @throws com.atlassian.jgitflow.core.exception.TagExistsException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireTagAbsent(String name) throws TagExistsException, JGitFlowGitAPIException
    {
        if (GitHelper.tagExists(git, name))
        {
            reporter.errorText(commandName, "requireTagAbsent() failed: '" + name + "' already exists");
            reporter.flush();
            throw new TagExistsException("tag '" + name + "' already exists");
        }
    }

    /**
     * Requires that the local branch with the given name is not behind a remote brach with the same name
     *
     * @param branch The name of the branch to test
     * @throws com.atlassian.jgitflow.core.exception.BranchOutOfDateException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public void requireLocalBranchNotBehindRemote(String branch) throws BranchOutOfDateException, JGitFlowIOException
    {
        reporter.debugMethod(commandName, "requireLocalBranchNotBehindRemote");
        boolean behind = GitHelper.localBranchBehindRemote(git, branch);

        if (behind)
        {
            reporter.errorText(commandName, "local branch '" + branch + "' is behind the remote branch");
            reporter.endMethod();
            reporter.flush();
            throw new BranchOutOfDateException("local branch '" + branch + "' is behind the remote branch");
        }

        reporter.endMethod();
    }

    /**
     * Requires that the local working tree has no un-committed changes
     *
     * @throws com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireCleanWorkingTree(boolean allowUntracked) throws DirtyWorkingTreeException, JGitFlowIOException, JGitFlowGitAPIException
    {
        CleanStatus cs = GitHelper.workingTreeIsClean(git, allowUntracked);
        if (cs.isNotClean())
        {
            reporter.errorText(commandName, cs.getMessage());
            reporter.flush();
            throw new DirtyWorkingTreeException(cs.getMessage());
        }
    }

    /**
     * Requires that no release branches already exist
     *
     * @throws com.atlassian.jgitflow.core.exception.ReleaseBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireNoExistingReleaseBranches() throws ReleaseBranchExistsException, JGitFlowGitAPIException
    {
        List<Ref> branches = GitHelper.listBranchesWithPrefix(git, gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.RELEASE.configKey()));

        if (!branches.isEmpty())
        {
            reporter.errorText(commandName, "a release branch [" + branches.get(0).getName() + "] already exists. Finish that first!");
            reporter.flush();
            throw new ReleaseBranchExistsException("a release branch [" + branches.get(0).getName() + "] already exists. Finish that first!");
        }
    }

    /**
     * Requires that no hotfix branches already exist
     *
     * @throws com.atlassian.jgitflow.core.exception.HotfixBranchExistsException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public void requireNoExistingHotfixBranches() throws HotfixBranchExistsException, JGitFlowGitAPIException
    {
        List<Ref> branches = GitHelper.listBranchesWithPrefix(git, gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.HOTFIX.configKey()));

        if (!branches.isEmpty())
        {
            reporter.errorText(commandName, "a hotfix branch [" + branches.get(0).getName() + "] already exists. Finish that first!");
            reporter.flush();
            throw new HotfixBranchExistsException("a hotfix branch [" + branches.get(0).getName() + "] already exists. Finish that first!");
        }
    }

    /**
     * Requires that a local branch contains the given commit
     *
     * @param commit the commit to test
     * @param branch the name of the branch to check
     */
    public void requireCommitOnBranch(RevCommit commit, String branch) throws LocalBranchExistsException, JGitFlowGitAPIException, JGitFlowIOException
    {
        if (!GitHelper.isMergedInto(git, commit, branch))
        {
            reporter.errorText(commandName, "requireCommitOnBranch() failed: '" + commit.getName() + "' is not on " + branch);
            reporter.flush();
            throw new LocalBranchExistsException("commit '" + commit.getName() + "' does not exist on " + branch);
        }
    }
}
