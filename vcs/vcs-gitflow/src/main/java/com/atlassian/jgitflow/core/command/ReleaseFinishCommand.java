package com.atlassian.jgitflow.core.command;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.ReleaseMergeResult;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.ReleaseFinishExtension;
import com.atlassian.jgitflow.core.extension.impl.EmptyReleaseFinishExtension;
import com.atlassian.jgitflow.core.extension.impl.MergeProcessExtensionWrapper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jgitflow.core.util.Preconditions.checkState;

/**
 * Finish a release.
 * <p>
 * This will merge the release into both master and develop and create a tag for the release
 * </p>
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Finish a release:
 * <p></p>
 * <pre>
 * flow.releaseFinish(&quot;1.0&quot;).call();
 * </pre>
 * <p></p>
 * Don't delete the local release branch
 * <p></p>
 * <pre>
 * flow.releaseFinish(&quot;1.0&quot;).setKeepBranch(true).call();
 * </pre>
 * <p></p>
 * Squash all commits on the release branch into one before merging
 * <p></p>
 * <pre>
 * flow.releaseFinish(&quot;1.0&quot;).setSquash(true).call();
 * </pre>
 * <p></p>
 * Push changes to the remote origin
 * <p></p>
 * <pre>
 * flow.releaseFinish(&quot;1.0&quot;).setPush(true).call();
 * </pre>
 * <p></p>
 * Don't create a tag for the release
 * <p></p>
 * <pre>
 * flow.releaseFinish(&quot;1.0&quot;).setNoTag(true).call();
 * </pre>
 */
public class ReleaseFinishCommand extends AbstractBranchMergingCommand<ReleaseFinishCommand, ReleaseMergeResult>
{
    private static final Logger log = LoggerFactory.getLogger(ReleaseFinishCommand.class);

    private static final String SHORT_NAME = "release-finish";
    private boolean noTag;
    private boolean squash;
    private boolean noMerge;
    private ReleaseFinishExtension extension;

    /**
     * Create a new release finish command instance.
     * <p></p>
     * An instance of this class is usually obtained by calling {@link com.atlassian.jgitflow.core.JGitFlow#releaseFinish(String)}
     *
     * @param releaseName The name/version of the release
     * @param git         The git instance to use
     * @param gfConfig    The GitFlowConfiguration to use
     */
    public ReleaseFinishCommand(String releaseName, Git git, GitFlowConfiguration gfConfig)
    {
        super(releaseName, git, gfConfig);
        checkState(!StringUtils.isEmptyOrNull(releaseName));
        this.noTag = false;
        this.squash = false;
        this.noMerge = false;
        this.extension = new EmptyReleaseFinishExtension();
    }

    /**
     * @return nothing
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchMissingException
     * @throws com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.BranchOutOfDateException
     */
    @Override
    public ReleaseMergeResult call() throws JGitFlowGitAPIException, LocalBranchMissingException, DirtyWorkingTreeException, JGitFlowIOException, BranchOutOfDateException, JGitFlowExtensionException, NotInitializedException
    {
        String prefixedBranchName = runBeforeAndGetPrefixedBranchName(extension.before(), JGitFlowConstants.PREFIXES.RELEASE);

        enforcer().requireGitFlowInitialized();
        enforcer().requireLocalBranchExists(prefixedBranchName);
        enforcer().requireCleanWorkingTree(isAllowUntracked());

        MergeResult developResult = createEmptyMergeResult();
        MergeResult masterResult = createEmptyMergeResult();
        try
        {
            doFetchIfNeeded(extension);


            ensureLocalBranchesNotBehindRemotes(prefixedBranchName, gfConfig.getMaster(), gfConfig.getDevelop());

            //checkout the branch to merge just so we can run any extensions that need to be on this branch
            checkoutTopicBranch(prefixedBranchName, extension);

            boolean mergeSuccess = false;
            if (!noMerge)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("merging topic branch to master...");
                }
                //first merge master
                MergeProcessExtensionWrapper masterExtension = new MergeProcessExtensionWrapper(extension.beforeMasterCheckout(), extension.afterMasterCheckout(), extension.beforeMasterMerge(), extension.afterMasterMerge());

                masterResult = doMerge(prefixedBranchName, gfConfig.getMaster(), masterExtension, squash);

                //now, tag master
                if (!noTag && masterResult.getMergeStatus().isSuccessful())
                {
                    doTag(gfConfig.getMaster(), getMessage(), masterResult, extension);
                }

                //IMPORTANT: we need to back-merge master into develop so that git describe works properly
                MergeProcessExtensionWrapper developExtension = new MergeProcessExtensionWrapper(extension.beforeDevelopCheckout(), extension.afterDevelopCheckout(), extension.beforeDevelopMerge(), extension.afterDevelopMerge());

                if (log.isDebugEnabled())
                {
                    log.debug("back merging master to develop...");
                }

                developResult = doMerge(gfConfig.getMaster(), gfConfig.getDevelop(), developExtension, squash);

                mergeSuccess = checkMergeResults(masterResult, developResult);

                if (mergeSuccess)
                {
                    doPushIfNeeded(extension, !noTag, gfConfig.getDevelop(), gfConfig.getMaster(), prefixedBranchName);
                }
            }

            if (noMerge || mergeSuccess)
            {
                cleanupBranchesIfNeeded(gfConfig.getDevelop(), prefixedBranchName);
            }

            if (log.isDebugEnabled())
            {
                log.debug("checking out develop...");
            }

            reporter.infoText(getCommandName(), "checking out '" + gfConfig.getDevelop() + "'");
            git.checkout().setName(gfConfig.getDevelop()).call();

            runExtensionCommands(extension.after());
            return new ReleaseMergeResult(masterResult, developResult);
        }
        catch (GitAPIException e)
        {
            reporter.errorText(getCommandName(), e.getMessage());
            throw new JGitFlowGitAPIException(e);
        }
        finally
        {
            reporter.endCommand();
            reporter.flush();
        }
    }


    /**
     * Set whether to turn off tagging
     *
     * @param noTag {@code true} to turn off tagging, {@code false}(default) otherwise
     * @return {@code this}
     */
    public ReleaseFinishCommand setNoTag(boolean noTag)
    {
        this.noTag = noTag;
        return this;
    }

    /**
     * Set whether to squash all commits into a single commit before the merge
     *
     * @param squash {@code true} to squash, {@code false}(default) otherwise
     * @return {@code this}
     */
    public ReleaseFinishCommand setSquash(boolean squash)
    {
        this.squash = squash;
        return this;
    }

    /**
     * Set whether to turn off merging
     *
     * @param noMerge {@code true} to turn off merging, {@code false}(default) otherwise
     * @return {@code this}
     */
    public ReleaseFinishCommand setNoMerge(boolean noMerge)
    {
        this.noMerge = noMerge;
        return this;
    }

    public ReleaseFinishCommand setExtension(ReleaseFinishExtension extension)
    {
        this.extension = extension;
        return this;
    }

    @Override
    protected String getCommandName()
    {
        return SHORT_NAME;
    }
}
