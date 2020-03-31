package com.atlassian.jgitflow.core.command;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;
import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.exception.JGitFlowIOException;
import com.atlassian.jgitflow.core.exception.LocalBranchMissingException;
import com.atlassian.jgitflow.core.extension.BranchMergingExtension;
import com.atlassian.jgitflow.core.extension.impl.MergeProcessExtensionWrapper;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.StringUtils;

public abstract class AbstractBranchMergingCommand<C, T> extends AbstractGitFlowCommand<C, T>
{
    private boolean keepBranch;
    private boolean forceDeleteBranch;
    private String message;

    protected AbstractBranchMergingCommand(String branchName, Git git, GitFlowConfiguration gfConfig)
    {
        super(branchName, git, gfConfig);
        this.forceDeleteBranch = true;
        this.message = "tagging release " + branchName;
    }

    protected MergeResult doMerge(String branchToMerge, String mergeTarget, MergeProcessExtensionWrapper extension) throws LocalBranchMissingException, JGitFlowGitAPIException, JGitFlowIOException, GitAPIException, JGitFlowExtensionException
    {
        return doMerge(branchToMerge, mergeTarget, extension, false);
    }

    protected MergeResult doMerge(String branchToMerge, String mergeTarget, MergeProcessExtensionWrapper extension, boolean squash) throws LocalBranchMissingException, JGitFlowGitAPIException, JGitFlowIOException, GitAPIException, JGitFlowExtensionException
    {
        return doMerge(branchToMerge, mergeTarget, extension, squash, MergeCommand.FastForwardMode.NO_FF);
    }

    protected MergeResult doMerge(String branchToMerge, String mergeTarget, MergeProcessExtensionWrapper extension, boolean squash, MergeCommand.FastForwardMode ffMode) throws LocalBranchMissingException, JGitFlowGitAPIException, JGitFlowIOException, GitAPIException, JGitFlowExtensionException
    {
        MergeResult mergeResult = createEmptyMergeResult();

        runExtensionCommands(extension.beforeCheckout());

        git.checkout().setName(mergeTarget).call();

        runExtensionCommands(extension.afterCheckout());

        if (!GitHelper.isMergedInto(git, branchToMerge, mergeTarget))
        {
            reporter.infoText(getCommandName(), "merging '" + branchToMerge + "' into '" + mergeTarget + "'...");

            runExtensionCommands(extension.beforeMerge());

            Ref localBranchRef = GitHelper.getLocalBranch(git, branchToMerge);
            if (squash)
            {
                reporter.infoText(getCommandName(), "squashing merge");
                mergeResult = git.merge().setSquash(true).include(localBranchRef).call();
                if (mergeResult.getMergeStatus().isSuccessful())
                {
                    git.commit().setMessage(getScmMessagePrefix() + "squashing '" + branchToMerge + "' into '" + mergeTarget + "'" + getScmMessageSuffix()).call();
                }
                this.forceDeleteBranch = true;
            }
            else
            {
                MergeCommand mergeCommand = git.merge().setFastForward(ffMode).include(localBranchRef);
                
                // check if you want scmCommentSuffix/scmCommentPrefix in the comment (if either is set, it shuold be used for merge messages as well for consistency)
                boolean isCustomScmMessage = (!StringUtils.isEmptyOrNull(getScmMessagePrefix())) || (!StringUtils.isEmptyOrNull(getScmMessageSuffix()));
                
                if(isCustomScmMessage) {
                  mergeCommand.setCommit(false);
                }
                
                mergeResult = mergeCommand.call();
                

                if (mergeResult.getMergeStatus().isSuccessful() && (MergeCommand.FastForwardMode.FF.equals(ffMode) || isCustomScmMessage))
                {
                    git.commit().setMessage(getScmMessagePrefix() + "merging '" + branchToMerge + "' into '" + mergeTarget + "'" + getScmMessageSuffix()).call();
                }
            }

            runExtensionCommands(extension.afterMerge());
        }

        reporter.mergeResult(getCommandName(), mergeResult);

        if (!mergeResult.getMergeStatus().isSuccessful())
        {
            reporter.errorText(getCommandName(), "merge into '" + mergeTarget + "' was not successful! Aborting...");
            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING))
            {
                reporter.errorText(getCommandName(), "please resolve your merge conflicts and re-run " + getCommandName());
            }
            else
            {
                reporter.errorText(getCommandName(), "until JGit supports merge resets, please run 'git reset --merge' to get back to a clean state");
            }
        }

        return mergeResult;
    }

    protected void doTag(String branchToTag, String tagMessage, MergeResult resultToLog, BranchMergingExtension extension) throws GitAPIException, JGitFlowGitAPIException, JGitFlowExtensionException
    {
        runExtensionCommands(extension.beforeTag());
        git.checkout().setName(branchToTag).call();
        String tagName = gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.VERSIONTAG.configKey()) + getBranchName();

        if (!GitHelper.tagExists(git, tagName))
        {
            reporter.infoText(
                    getCommandName(),
                    String.format(
                            "tagging with name: <%s>. merge status (%s)",
                            tagName,
                            resultToLog.getMergeStatus()
                    )
            );
            git.tag().setName(tagName).setMessage(getScmMessagePrefix() + tagMessage + getScmMessageSuffix()).call();
        }

        runExtensionCommands(extension.afterTag());
    }

    protected void cleanupBranchesIfNeeded(String branchToCheckout, String... branchesToDelete) throws GitAPIException, JGitFlowGitAPIException
    {
        if (!keepBranch)
        {
            git.checkout().setName(branchToCheckout).call();

            for (String branchToDelete : branchesToDelete)
            {
                if (GitHelper.localBranchExists(git, branchToDelete))
                {
                    reporter.infoText(getCommandName(), "deleting local branch: " + branchToDelete);

                    git.branchDelete().setForce(forceDeleteBranch).setBranchNames(branchToDelete).call();
                }

                if (isPush() && GitHelper.remoteBranchExists(git, branchToDelete))
                {
                    reporter.infoText(getCommandName(), "pushing deleted branch: :" + branchToDelete);
                    RefSpec deleteSpec = new RefSpec().setSource(null).setDestination(Constants.R_HEADS + branchToDelete);
                    git.push().setRemote(Constants.DEFAULT_REMOTE_NAME).setRefSpecs(deleteSpec).call();
                }
            }
        }
    }

    protected void checkoutTopicBranch(String branchName, BranchMergingExtension extension) throws GitAPIException, JGitFlowExtensionException
    {
        git.checkout().setName(branchName).call();
        runExtensionCommands(extension.afterTopicCheckout());
    }

    protected MergeResult createEmptyMergeResult()
    {
        return new MergeResult(null, null, new ObjectId[]{null, null}, MergeResult.MergeStatus.ALREADY_UP_TO_DATE, MergeStrategy.RESOLVE, null);
    }

    protected boolean checkMergeResults(MergeResult... resultsToCheck)
    {
        boolean isSuccess = true;
        for (MergeResult result : resultsToCheck)
        {
            if (!result.getMergeStatus().isSuccessful())
            {
                isSuccess = false;
                break;
            }
        }

        return isSuccess;
    }

    public C setKeepBranch(boolean keepBranch)
    {
        this.keepBranch = keepBranch;
        return (C) this;
    }

    /**
     * Set whether to use the force flag when deleting the local feature branch
     *
     * @param force {@code true} to force, {@code false}(default) otherwise
     * @return {@code this}
     */
    public C setForceDeleteBranch(boolean force)
    {
        this.forceDeleteBranch = force;
        return (C) this;
    }

    /**
     * Set the commit message for the tag creation
     *
     * @param message
     * @return {@code this}
     */
    public C setMessage(String message)
    {
        this.message = message;
        return (C) this;
    }

    public boolean isForceDeleteBranch()
    {
        return forceDeleteBranch;
    }

    public boolean isKeepBranch()
    {
        return keepBranch;
    }

    public String getMessage()
    {
        return message;
    }
}
