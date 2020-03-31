package com.atlassian.jgitflow.core.command;

import java.io.IOException;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.BranchCreatingExtension;
import com.atlassian.jgitflow.core.extension.JGitFlowExtension;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.StringUtils;

public abstract class AbstractBranchCreatingCommand<C, T> extends AbstractGitFlowCommand<C, T>
{
    private RevCommit startCommit;
    private String startCommitString;

    protected AbstractBranchCreatingCommand(String branchName, Git git, GitFlowConfiguration gfConfig)
    {
        super(branchName, git, gfConfig);
        this.startCommit = null;
        this.startCommitString = null;
    }

    protected Ref doCreateBranch(String rootBranch, String newBranchName, BranchCreatingExtension extension) throws JGitFlowExtensionException, JGitFlowIOException, LocalBranchMissingException, JGitFlowGitAPIException, BranchOutOfDateException, LocalBranchExistsException, TagExistsException, GitAPIException
    {
        git.checkout().setName(rootBranch).call();

        runExtensionCommands(extension.beforeCreateBranch());

        RevCommit startPoint = getStartingPoint(rootBranch);

        RevCommit latest = GitHelper.getLatestCommit(git, rootBranch);
        reporter.debugText(getCommandName(), "startPoint is: " + startPoint);
        reporter.debugText(getCommandName(), "latestCommit is: " + latest.getName());

        if (GitHelper.remoteBranchExists(git, rootBranch))
        {
            enforcer().requireLocalBranchNotBehindRemote(rootBranch);
        }

        enforcer().requireCommitOnBranch(startPoint, rootBranch);

        enforcer().requireTagAbsent(gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.VERSIONTAG.configKey()) + getBranchName());


        Ref newBranch = git.checkout()
                           .setName(newBranchName)
                           .setCreateBranch(true)
                           .setStartPoint(startPoint)
                           .call();

        reporter.debugText(getCommandName(), "created branch: " + newBranchName);

        runExtensionCommands(extension.afterCreateBranch());

        return newBranch;
    }

    protected void doPushNewBranchIfNeeded(JGitFlowExtension pushExtension, String branchToPush) throws GitAPIException, JGitFlowGitAPIException, JGitFlowExtensionException, RemoteBranchExistsException, IOException
    {
        if (isPush())
        {
            enforcer().requireRemoteBranchAbsent(branchToPush);
            reporter.infoText(getCommandName(), "pushing new branch to origin: " + branchToPush);

            git.push().setRemote(Constants.DEFAULT_REMOTE_NAME).setRefSpecs(new RefSpec(gfConfig.getDevelop()), new RefSpec(branchToPush)).call();

            reporter.debugText(getCommandName(), "push complete");

            git.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME).call();

            //setup tracking
            StoredConfig config = git.getRepository().getConfig();
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchToPush, ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME);
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branchToPush, ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + branchToPush);
            config.save();
            try
            {
                config.load();
            }
            catch (ConfigInvalidException e)
            {
                throw new JGitFlowGitAPIException("unable to load config", e);
            }
            runExtensionCommands(pushExtension.afterPush());
        }
    }

    protected RevCommit getStartingPoint(String fromBranch) throws JGitFlowIOException, LocalBranchMissingException
    {
        RevCommit startPoint = null;

        if (null != startCommit)
        {
            startPoint = startCommit;
        }
        else if (!StringUtils.isEmptyOrNull(startCommitString))
        {
            startPoint = GitHelper.getCommitForString(git, startCommitString);
        }
        else
        {
            startPoint = GitHelper.getLatestCommit(git, fromBranch);
        }

        return startPoint;
    }

    public C setStartCommit(String commitId)
    {
        this.startCommitString = commitId;

        return (C) this;
    }

    public C setStartCommit(RevCommit commit)
    {
        this.startCommit = commit;

        return (C) this;
    }
}
