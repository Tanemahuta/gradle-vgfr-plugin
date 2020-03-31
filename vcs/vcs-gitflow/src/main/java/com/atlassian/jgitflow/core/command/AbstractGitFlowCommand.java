package com.atlassian.jgitflow.core.command;

import java.util.concurrent.Callable;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.JGitFlowReporter;
import com.atlassian.jgitflow.core.exception.BranchOutOfDateException;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;
import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.exception.JGitFlowIOException;
import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy;
import com.atlassian.jgitflow.core.extension.JGitFlowExtension;
import com.atlassian.jgitflow.core.util.GitHelper;
import com.atlassian.jgitflow.core.util.RequirementHelper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jgitflow.core.util.Preconditions.checkNotNull;

/**
 * The base class for all JGitFlow commands.
 * <p>
 * Most commands should extend this class as it provides common helper methods
 * and methods to ensure valid state.
 * </p>
 *
 * @param <T> The return type of the call() method
 */
public abstract class AbstractGitFlowCommand<C, T> implements Callable<T>, JGitFlowCommand
{
    private static final Logger log = LoggerFactory.getLogger(AbstractGitFlowCommand.class);
    protected final Git git;
    protected final GitFlowConfiguration gfConfig;
    protected final JGitFlowReporter reporter = JGitFlowReporter.get();
    protected final RequirementHelper requirementHelper;
    private boolean allowUntracked;
    private String scmMessagePrefix;
    private String scmMessageSuffix;
    private boolean fetch;
    private boolean push;
    private final String branchName;

    protected AbstractGitFlowCommand(String branchName, Git git, GitFlowConfiguration gfConfig)
    {
        checkNotNull(branchName);
        checkNotNull(git);
        checkNotNull(gfConfig);

        this.requirementHelper = new RequirementHelper(git, gfConfig, getCommandName());

        this.git = git;
        this.gfConfig = gfConfig;
        this.allowUntracked = false;
        this.scmMessagePrefix = "";
        this.scmMessageSuffix = "";
        this.fetch = false;
        this.push = false;
        this.branchName = branchName;
    }

    protected void doFetchIfNeeded(JGitFlowExtension fetchingExtension) throws GitAPIException, JGitFlowGitAPIException, JGitFlowExtensionException
    {
        if (fetch)
        {
            runExtensionCommands(fetchingExtension.beforeFetch());

            git.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME).call();

            runExtensionCommands(fetchingExtension.afterFetch());
        }
    }

    protected void doPushIfNeeded(JGitFlowExtension pushExtension, boolean includeTags, String... branchesToPush) throws GitAPIException, JGitFlowGitAPIException, JGitFlowExtensionException
    {
        if (push)
        {
            reporter.infoText(getCommandName(), "pushing changes to origin...");

            for (String branchToPush : branchesToPush)
            {
                if (GitHelper.remoteBranchExists(git, branchToPush))
                {
                    reporter.infoText(getCommandName(), "pushing '" + branchToPush + "'");
                    RefSpec branchSpec = new RefSpec(branchToPush);
                    Iterable<PushResult> i = git.push().setRemote(Constants.DEFAULT_REMOTE_NAME).setRefSpecs(branchSpec).call();
                    for (PushResult pr : i)
                    {
                        reporter.infoText(getCommandName(), "messages: '" + pr.getMessages() + "'");
                        
                        if(null != pr && null != pr.getRemoteUpdates() && !pr.getRemoteUpdates().isEmpty()) {
                            for (RemoteRefUpdate update : pr.getRemoteUpdates()) {

                                RemoteRefUpdate.Status trackingStatus = update.getStatus();
                                if (failedResult(trackingStatus)) {
                                    if (pr.getMessages() != null && pr.getMessages().length() > 0) {
                                        throw new JGitFlowGitAPIException("error pushing to " + branchToPush + " - status: " + trackingStatus.name() + " - " + pr.getMessages());
                                    } else {
                                        throw new JGitFlowGitAPIException("error pushing to " + branchToPush + " - " + trackingStatus.name());
                                    }
                                }
                            }
                        }
                        else
                        {
                            throw new JGitFlowGitAPIException("error pushing to " + branchToPush + " - " + pr.getMessages());
                        }
                    }
                }
            }

            if (includeTags)
            {
                reporter.infoText(getCommandName(), "pushing tags");
                // TODO: check for errors or at least log error messages received
                git.push().setRemote(Constants.DEFAULT_REMOTE_NAME).setPushTags().call();
            }

            git.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME).call();

            runExtensionCommands(pushExtension.afterPush());
        }
    }

    private boolean failedResult(RemoteRefUpdate.Status trackingStatus) {
        boolean isFailed = false;
        
        switch(trackingStatus)
        {
            case REJECTED_NONFASTFORWARD:
            case REJECTED_NODELETE:
            case REJECTED_REMOTE_CHANGED:
            case REJECTED_OTHER_REASON:
                isFailed = true;
                break;
        }
        
        return isFailed;
    }

    protected String runBeforeAndGetPrefixedBranchName(Iterable<ExtensionCommand> before, JGitFlowConstants.PREFIXES prefix) throws JGitFlowExtensionException
    {
        reporter.commandCall(getCommandName());
        runExtensionCommands(before);
        return gfConfig.getPrefixValue(prefix.configKey()) + branchName;
    }

    protected void ensureLocalBranchesNotBehindRemotes(String... branchesToTest) throws JGitFlowGitAPIException, BranchOutOfDateException, JGitFlowIOException
    {
        for (String branchToTest : branchesToTest)
        {
            if (GitHelper.remoteBranchExists(git, branchToTest))
            {
                enforcer().requireLocalBranchNotBehindRemote(branchToTest);
            }
        }
    }

    @Override
    public C setAllowUntracked(boolean allow)
    {
        this.allowUntracked = allow;
        return (C) this;
    }

    @Override
    public boolean isAllowUntracked()
    {
        return allowUntracked;
    }

    @Override
    public String getScmMessagePrefix()
    {
        return scmMessagePrefix;
    }

    @Override
    public C setScmMessagePrefix(String scmMessagePrefix)
    {
        this.scmMessagePrefix = scmMessagePrefix;
        return (C) this;
    }

    @Override
    public String getScmMessageSuffix()
    {
        return scmMessageSuffix;
    }

    @Override
    public C setScmMessageSuffix(String scmMessageSuffix)
    {
        this.scmMessageSuffix = scmMessageSuffix;
        return (C) this;
    }

    /**
     * Set whether to perform a git fetch of the remote branches before doing the merge
     *
     * @param fetch {@code true} to do the fetch, {@code false}(default) otherwise
     * @return {@code this}
     */
    @Override
    public C setFetch(boolean fetch)
    {
        this.fetch = fetch;
        return (C) this;
    }

    @Override
    public boolean isFetch()
    {
        return fetch;
    }

    /**
     * Set whether to push the changes to the remote repository
     *
     * @param push {@code true} to do the push, {@code false}(default) otherwise
     * @return {@code this}
     */
    @Override
    public C setPush(boolean push)
    {
        this.push = push;
        return (C) this;
    }

    @Override
    public boolean isPush()
    {
        return push;
    }

    @Override
    public String getBranchName()
    {
        return branchName;
    }

    protected abstract String getCommandName();

    protected void runExtensionCommands(Iterable<ExtensionCommand> commands) throws JGitFlowExtensionException
    {
        for (final ExtensionCommand command : commands)
        {
            try
            {
                command.execute(gfConfig, git, this);
            }
            catch (JGitFlowExtensionException e)
            {
                if (ExtensionFailStrategy.ERROR.equals(command.failStrategy()))
                {
                    throw e;
                }
                else
                {
                    log.warn("Error running JGitFlow Extension", e);
                }
            }
        }
    }

    protected RequirementHelper enforcer()
    {
        return requirementHelper;
    }
}
