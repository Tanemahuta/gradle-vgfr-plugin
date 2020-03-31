package com.atlassian.jgitflow.core.command;

import java.util.List;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.ReleaseMergeResult;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.extension.HotfixFinishExtension;
import com.atlassian.jgitflow.core.extension.impl.EmptyHotfixFinishExtension;
import com.atlassian.jgitflow.core.extension.impl.MergeProcessExtensionWrapper;
import com.atlassian.jgitflow.core.util.GitHelper;

import com.atlassian.jgitflow.core.util.RequirementHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.util.StringUtils;

import static com.atlassian.jgitflow.core.util.Preconditions.checkState;

/**
 * Finish a hotfix.
 * <p>
 * This will merge the hotfix into both master and develop and create a tag for the hotfix
 * </p>
 * <p></p>
 * Examples ({@code flow} is a {@link com.atlassian.jgitflow.core.JGitFlow} instance):
 * <p></p>
 * Finish a hotfix:
 * <p></p>
 * <pre>
 * flow.hotfixFinish(&quot;1.0&quot;).call();
 * </pre>
 * <p></p>
 * Don't delete the local hotfix branch
 * <p></p>
 * <pre>
 * flow.hotfixFinish(&quot;1.0&quot;).setKeepBranch(true).call();
 * </pre>
 * <p></p>
 * Squash all commits on the hotfix branch into one before merging
 * <p></p>
 * <pre>
 * flow.hotfixFinish(&quot;1.0&quot;).setSquash(true).call();
 * </pre>
 * <p></p>
 * Push changes to the remote origin
 * <p></p>
 * <pre>
 * flow.hotfixFinish(&quot;1.0&quot;).setPush(true).call();
 * </pre>
 * <p></p>
 * Don't create a tag for the hotfix
 * <p></p>
 * <pre>
 * flow.hotfixFinish(&quot;1.0&quot;).setNoTag(true).call();
 * </pre>
 */
public class HotfixFinishCommand extends AbstractBranchMergingCommand<HotfixFinishCommand, ReleaseMergeResult>
{
    private static final String SHORT_NAME = "hotfix-finish";
    private String message;
    private boolean noTag;
    private HotfixFinishExtension extension;

    /**
     * Create a new hotfix finish command instance.
     * <p></p>
     * An instance of this class is usually obtained by calling {@link com.atlassian.jgitflow.core.JGitFlow#hotfixFinish(String)}
     *
     * @param hotfixName The name/version of the hotfix
     * @param git        The git instance to use
     * @param gfConfig   The GitFlowConfiguration to use
     */
    public HotfixFinishCommand(String hotfixName, Git git, GitFlowConfiguration gfConfig)
    {
        super(hotfixName, git, gfConfig);

        checkState(!StringUtils.isEmptyOrNull(hotfixName));
        this.message = "tagging hotfix " + hotfixName;
        this.noTag = false;
        this.extension = new EmptyHotfixFinishExtension();
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
        String prefixedBranchName = runBeforeAndGetPrefixedBranchName(extension.before(), JGitFlowConstants.PREFIXES.HOTFIX);

        enforcer().requireGitFlowInitialized();
        enforcer().requireLocalBranchExists(prefixedBranchName);
        enforcer().requireCleanWorkingTree(isAllowUntracked());

        MergeResult developResult = createEmptyMergeResult();
        MergeResult masterResult = createEmptyMergeResult();
        MergeResult releaseResult = createEmptyMergeResult();
        try
        {
            doFetchIfNeeded(extension);

            ensureLocalBranchesNotBehindRemotes(prefixedBranchName, gfConfig.getMaster(), gfConfig.getDevelop());

            //checkout the branch to merge just so we can run any extensions that need to be on this branch
            checkoutTopicBranch(prefixedBranchName, extension);

            //first merge master
            MergeProcessExtensionWrapper masterExtension = new MergeProcessExtensionWrapper(extension.beforeMasterCheckout(), extension.afterMasterCheckout(), extension.beforeMasterMerge(), extension.afterMasterMerge());
            masterResult = doMerge(prefixedBranchName, gfConfig.getMaster(), masterExtension);

            //now, tag master
            if (!noTag && masterResult.getMergeStatus().isSuccessful())
            {
                doTag(gfConfig.getMaster(), message, masterResult, extension);
            }

            //IMPORTANT: we need to back-merge master into develop so that git describe works properly
            MergeProcessExtensionWrapper developExtension = new MergeProcessExtensionWrapper(extension.beforeDevelopCheckout(), extension.afterDevelopCheckout(), extension.beforeDevelopMerge(), extension.afterDevelopMerge());

            developResult = doMerge(gfConfig.getMaster(), gfConfig.getDevelop(), developExtension);

            boolean mergeSuccess = checkMergeResults(masterResult, developResult);

            if (mergeSuccess)
            {
                doPushIfNeeded(extension, !noTag, gfConfig.getDevelop(), gfConfig.getMaster(), prefixedBranchName);
            }

            //Backmerge to release branch if needed
            if (releaseBranchExists())
            {
                String releaseBranchName = getReleaseBranchName();
                requirementHelper.requireLocalBranchExists(releaseBranchName);
                
                MergeProcessExtensionWrapper releaseExtension = new MergeProcessExtensionWrapper(extension.beforeReleaseCheckout(), extension.afterReleaseCheckout(), extension.beforeReleaseMerge(), extension.afterReleaseMerge());

                releaseResult = doMerge(gfConfig.getMaster(), releaseBranchName, releaseExtension);

                boolean releaseMergeSuccess = checkMergeResults(releaseResult);

                if (releaseMergeSuccess)
                {
                    doPushIfNeeded(extension, !noTag, releaseBranchName);
                }
            }

            if (mergeSuccess)
            {
                cleanupBranchesIfNeeded(gfConfig.getDevelop(), prefixedBranchName);
            }
            
            reporter.infoText(getCommandName(), "checking out '" + gfConfig.getDevelop() + "'");
            git.checkout().setName(gfConfig.getDevelop()).call();

            runExtensionCommands(extension.after());
            return new ReleaseMergeResult(masterResult, developResult);

        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
        finally
        {
            reporter.endCommand();
            reporter.flush();
        }
    }

    private boolean releaseBranchExists() throws JGitFlowGitAPIException
    {
        boolean exists = false;

        List<Ref> branches = GitHelper.listBranchesWithPrefix(git, gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.RELEASE.configKey()));

        if (!branches.isEmpty())
        {
            exists = true;
        }

        return exists;
    }

    private String getReleaseBranchName() throws JGitFlowGitAPIException
    {
        String branchName = "";

        List<Ref> branches = GitHelper.listBranchesWithPrefix(git, gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.RELEASE.configKey()));

        if (!branches.isEmpty())
        {
            branchName = branches.get(0).getName();
        }

        return branchName.substring(branchName.indexOf(gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.RELEASE.configKey())));
    }

    /**
     * Set the commit message for the tag creation
     *
     * @param message
     * @return {@code this}
     */
    public HotfixFinishCommand setMessage(String message)
    {
        this.message = message;
        return this;
    }

    /**
     * Set whether to turn off tagging
     *
     * @param noTag {@code true} to turn off tagging, {@code false}(default) otherwise
     * @return {@code this}
     */
    public HotfixFinishCommand setNoTag(boolean noTag)
    {
        this.noTag = noTag;
        return this;
    }

    public HotfixFinishCommand setExtension(HotfixFinishExtension extension)
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
