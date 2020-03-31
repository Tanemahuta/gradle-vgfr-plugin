package com.atlassian.jgitflow.core.util;

import java.io.IOException;
import java.util.*;

import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.JGitFlowReporter;
import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.exception.JGitFlowIOException;
import com.atlassian.jgitflow.core.exception.LocalBranchMissingException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.StringUtils;

import static com.atlassian.jgitflow.core.util.Preconditions.checkNotNull;

/**
 * A helper class for common Git operations
 */
public class GitHelper
{
    /**
     * Checks to see if one branch is merged into another
     *
     * @param git          The git instance to use
     * @param commitString The name of the commit we're testing
     * @param baseBranch   The name of the base branch to look for the merge
     * @return if the contents of branchName has been merged into baseName
     * @throws com.atlassian.jgitflow.core.exception.LocalBranchMissingException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static boolean isMergedInto(Git git, String commitString, String baseBranch) throws LocalBranchMissingException, JGitFlowIOException, JGitFlowGitAPIException
    {
        RevCommit branchCommit = getCommitForString(git, commitString);

        return isMergedInto(git, branchCommit, baseBranch);
    }

    /**
     * Gets a commit for a given string with no body
     *
     * @param git      The git instance to use
     * @param commitId The name of the commit to find
     * @return The commit
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static RevCommit getCommitForString(Git git, String commitId) throws JGitFlowIOException, LocalBranchMissingException
    {
        RevWalk walk = null;
        try
        {
            ObjectId commit = git.getRepository().resolve(commitId);

            if (null == commit)
            {
                throw new LocalBranchMissingException("commit " + commitId + " does not exist");
            }

            walk = new RevWalk(git.getRepository());
            walk.setRetainBody(true);

            return walk.parseCommit(commit);
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
        finally
        {
            if (null != walk)
            {
                walk.close();
            }
        }
    }

    /**
     * Checks to see if a specific commit is merged into a branch
     *
     * @param git        The git instance to use
     * @param commit     The commit to test
     * @param branchName The name of the base branch to look for the merge
     * @return if the contents of commit has been merged into baseName
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static boolean isMergedInto(Git git, RevCommit commit, String branchName) throws JGitFlowGitAPIException, JGitFlowIOException
    {
        Repository repo = git.getRepository();
        try
        {
            ObjectId base = repo.resolve(branchName);

            if (null == base)
            {
                return false;
            }

            Iterable<RevCommit> baseCommits = git.log().add(base).call();

            boolean merged = false;

            for (RevCommit entry : baseCommits)
            {
                if (entry.getId().equals(commit))
                {
                    merged = true;
                    break;
                }
                if (entry.getParentCount() > 1 && Arrays.asList(entry.getParents()).contains(commit))
                {
                    merged = true;
                    break;
                }
            }

            return merged;
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }

    }

    /**
     * Gets the latest commit for a branch
     *
     * @param git        The git instance to use
     * @param branchName The name of the branch to find the commit on
     * @return The latest commit for the branch
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static RevCommit getLatestCommit(Git git, String branchName) throws JGitFlowIOException
    {
        RevWalk walk = null;
        try
        {
            ObjectId branch = git.getRepository().resolve(branchName);
            walk = new RevWalk(git.getRepository());
            walk.setRetainBody(true);

            return walk.parseCommit(branch);
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
        finally
        {
            if (null != walk)
            {
                walk.close();
            }
        }
    }

    /**
     * Checks to see if a local branch with the given name exists
     *
     * @param git        The git instance to use
     * @param branchName The name of the branch to look for
     * @return if the branch exists or not
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static boolean localBranchExists(Git git, String branchName) throws JGitFlowGitAPIException
    {
        boolean exists = false;

        if (StringUtils.isEmptyOrNull(branchName))
        {
            return exists;
        }

        try
        {
            List<Ref> refs = git.branchList().setListMode(null).call();
            for (Ref ref : refs)
            {
                if (ref.getName().contains(Constants.R_HEADS)) {
                    String simpleName = ref.getName().substring(ref.getName().indexOf(Constants.R_HEADS) + Constants.R_HEADS.length());
                    if (simpleName.equals(branchName))
                    {
                        exists = true;
                        break;
                    }
                }
            }

            return exists;
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
    }

    /**
     * Checks to see if a remote branch with the given name exists
     *
     * @param git    The git instance to use
     * @param branch The name of the branch to look for
     * @return if the branch exists or not
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static boolean remoteBranchExists(Git git, final String branch) throws JGitFlowGitAPIException
    {
        JGitFlowReporter reporter = JGitFlowReporter.get();
        reporter.debugMethod(getName(), "remoteBranchExists");
        reporter.debugText(getName(), "checking for branch: " + branch);
        boolean exists = false;

        if (StringUtils.isEmptyOrNull(branch))
        {
            return exists;
        }

        try
        {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            reporter.debugText(getName(), "got " + refs.size() + " remote refs");
            for (Ref ref : refs)
            {
                reporter.debugText(getName(), "ref name: " + ref.getName());

                //if we're not coming from origin, just ignore
                if (!ref.getName().contains(JGitFlowConstants.R_REMOTE_ORIGIN))
                {
                    continue;
                }

                String simpleName = ref.getName().substring(ref.getName().indexOf(JGitFlowConstants.R_REMOTE_ORIGIN) + JGitFlowConstants.R_REMOTE_ORIGIN.length());

                reporter.debugText(getName(), "ref simple name: " + simpleName);

                reporter.debugText(getName(), "simple name equals branch? " + simpleName.equals(branch));
                if (simpleName.equals(branch))
                {
                    exists = true;
                    break;
                }
            }

            return exists;
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
        finally
        {
            reporter.endMethod();
            reporter.flush();
        }
    }

    public static boolean localBranchBehindRemote(Git git, final String branch) throws JGitFlowIOException
    {
        JGitFlowReporter reporter = JGitFlowReporter.get();
        final RevWalk walk = new RevWalk(git.getRepository());
        walk.setRetainBody(true);
        boolean behind = false;
        try
        {
            Ref remote = getRemoteBranch(git, branch);
            Ref local = getLocalBranch(git, branch);

            checkNotNull(remote);
            checkNotNull(local);

            ObjectId remoteId = git.getRepository().resolve(remote.getObjectId().getName());
            RevCommit remoteCommit = walk.parseCommit(remoteId);
            RevCommit localCommit = walk.parseCommit(local.getObjectId());

            if (!localCommit.equals(remoteCommit))
            {
                reporter.debugText(getName(), localCommit.getName() + " !equals " + remoteCommit.getName());
                behind = true;
                walk.setRevFilter(RevFilter.MERGE_BASE);
                walk.markStart(localCommit);
                walk.markStart(remoteCommit);

                RevCommit base = walk.next();
                reporter.debugText(getName(), "checking if remote is at our merge base");
                if (null != base)
                {
                    walk.parseBody(base);

                    //remote is behind
                    if (remoteCommit.equals(base))
                    {
                        reporter.debugText(getName(), "remote equals merge base, branch is newer");
                        behind = false;
                    }
                }
            }
        }
        catch (IOException e)
        {
            reporter.errorText(getName(), e.getMessage());
            reporter.endMethod();
            reporter.flush();
            throw new JGitFlowIOException(e);
        }
        finally
        {
            walk.close();
        }

        return behind;
    }

    /**
     * Gets a reference to a remote branch with the given name
     *
     * @param git        The git instance to use
     * @param branchName The name of the remote branch
     * @return A reference to the remote branch or null
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static Ref getRemoteBranch(Git git, String branchName) throws JGitFlowIOException
    {
        try
        {
            final Map<String, Ref> refList = git.getRepository().getRefDatabase().getRefs(Constants.R_REMOTES);
            Ref remote = null;

            for (Map.Entry<String, Ref> entry : refList.entrySet())
            {
                int index = entry.getValue().getName().indexOf(JGitFlowConstants.R_REMOTE_ORIGIN);
                if (index < 0)
                {
                    continue;
                }

                String simpleName = entry.getValue().getName().substring(index + JGitFlowConstants.R_REMOTE_ORIGIN.length());
                if (simpleName.equals(branchName))
                {
                    remote = entry.getValue();
                    break;
                }
            }

            return remote;
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
    }

    /**
     * Gets a reference to a local branch with the given name
     *
     * @param git        The git instance to use
     * @param branchName The name of the remote branch
     * @return A reference to the local branch or null
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static Ref getLocalBranch(Git git, String branchName) throws JGitFlowIOException
    {
        try
        {
            Ref ref2check = git.getRepository().findRef(branchName);
            Ref local = null;
            if (ref2check != null && ref2check.getName().startsWith(Constants.R_HEADS))
            {
                local = ref2check;
            }

            return local;
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
    }

    /**
     * Gets a list of branch references that begin with the given prefix
     *
     * @param git    The git instance to use
     * @param prefix The prefix to test for
     * @return A list of branch references matching the given prefix
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static List<Ref> listBranchesWithPrefix(Git git, String prefix) throws JGitFlowGitAPIException
    {
        JGitFlowReporter reporter = JGitFlowReporter.get();
        List<Ref> branches = new ArrayList<Ref>();
        reporter.debugMethod(getName(), "listBranchesWithPrefix");

        try
        {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            for (Ref ref : refs)
            {
                String simpleName;

                String originPrefix = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/";

                if (ref.getName().indexOf(Constants.R_HEADS) > -1)
                {
                    simpleName = ref.getName().substring(ref.getName().indexOf(Constants.R_HEADS) + Constants.R_HEADS.length());
                }
                else if (ref.getName().indexOf(originPrefix) > -1)
                {
                    simpleName = ref.getName().substring(ref.getName().indexOf(originPrefix) + originPrefix.length());
                }
                else
                {
                    simpleName = "";
                }

                reporter.debugText(getName(), "simple name [" + simpleName + "] startsWith prefix [" + prefix + "] ? " + simpleName.startsWith(prefix));

                if (simpleName.startsWith(prefix))
                {
                    branches.add(ref);
                }
            }

            return branches;
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
        finally
        {
            reporter.endMethod();
        }
    }

    /**
     * Tests to see if a working folder is clean. e.g. all changes have been committed.
     *
     * @param git            The git instance to use
     * @param allowUntracked
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static CleanStatus workingTreeIsClean(Git git, boolean allowUntracked) throws JGitFlowIOException, JGitFlowGitAPIException
    {
        JGitFlowReporter reporter = JGitFlowReporter.get();

        reporter.debugMethod(getName(), "workingTreeIsClean");
        try
        {
            git.getRepository().getRefDatabase().refresh();
            IndexDiff diffIndex = new IndexDiff(git.getRepository(), Constants.HEAD, new FileTreeIterator(git.getRepository()));

            if (diffIndex.diff())
            {
                int addedSize = diffIndex.getAdded().size();
                int assumedSize = diffIndex.getAssumeUnchanged().size();
                int changedSize = diffIndex.getChanged().size();
                int conflictSize = diffIndex.getConflicting().size();
                int ignoredSize = diffIndex.getIgnoredNotInIndex().size();
                int missingSize = diffIndex.getMissing().size();
                int modifiedSize = diffIndex.getModified().size();
                int removedSize = diffIndex.getRemoved().size();
                int untrackedSize = diffIndex.getUntracked().size();
                int untrackedFolderSize = diffIndex.getUntrackedFolders().size();

                boolean changed = false;
                boolean untracked = false;
                StringBuilder sb = new StringBuilder();

                reporter.debugText(getName(), "diffIndex.diff() returned diffs. working tree is dirty!");
                reporter.debugText(getName(), "added size: " + addedSize);
                reportDirtyDetails(getName(), "added", diffIndex.getAdded());
                reporter.debugText(getName(), "assume unchanged size: " + assumedSize);
                reporter.debugText(getName(), "changed size: " + changedSize);
                reportDirtyDetails(getName(), "changed", diffIndex.getChanged());
                reporter.debugText(getName(), "conflicting size: " + conflictSize);
                reportDirtyDetails(getName(), "conflicting", diffIndex.getConflicting());
                reporter.debugText(getName(), "ignored not in index size: " + ignoredSize);
                reporter.debugText(getName(), "missing size: " + missingSize);
                reportDirtyDetails(getName(), "missing", diffIndex.getMissing());
                reporter.debugText(getName(), "modified size: " + modifiedSize);
                reportDirtyDetails(getName(), "modified", diffIndex.getModified());
                reporter.debugText(getName(), "removed size: " + removedSize);
                reportDirtyDetails(getName(), "removed", diffIndex.getRemoved());
                reporter.debugText(getName(), "untracked size: " + untrackedSize);
                reportDirtyDetails(getName(), "untracked", diffIndex.getUntracked());
                reporter.debugText(getName(), "untracked folders size: " + untrackedFolderSize);
                reportDirtyDetails(getName(), "untracked folders", diffIndex.getUntrackedFolders());
                reporter.endMethod();

                if (addedSize > 0 || changedSize > 0 || conflictSize > 0 || missingSize > 0 || modifiedSize > 0 || removedSize > 0)
                {
                    changed = true;
                    sb.append("Working tree has uncommitted changes");
                }

                if (!allowUntracked && (untrackedSize > 0 || untrackedFolderSize > 0))
                {
                    if (ignoredSize > 0)
                    {
                        Set<String> ignores = diffIndex.getIgnoredNotInIndex();

                        if (untrackedSize > 0)
                        {
                            Set<String> utFiles = diffIndex.getUntracked();
                            utFiles.removeAll(ignores);

                            untrackedSize = utFiles.size();
                        }

                        if (untrackedFolderSize > 0)
                        {
                            Set<String> utFolders = diffIndex.getUntrackedFolders();
                            utFolders.removeAll(ignores);

                            untrackedFolderSize = utFolders.size();
                        }
                    }

                    if (untrackedSize > 0 || untrackedFolderSize > 0)
                    {
                        untracked = true;
                    }

                    if (!changed)
                    {
                        sb.append("Working tree has untracked files");
                    }
                    else
                    {
                        sb.append(" and untracked files");
                    }
                }

                return new CleanStatus(untracked, changed, sb.toString());
            }

            reporter.debugText(getName(), "working tree is clean");
            reporter.endMethod();
            return new CleanStatus(false, false, "Working tree is clean");
        }
        catch (IOException e)
        {
            reporter.errorText(getName(), e.getMessage());
            reporter.endMethod();
            reporter.flush();
            throw new JGitFlowIOException(e);
        }
    }

    private static void reportDirtyDetails(String cmdName, String reason, Set<String> files)
    {
        JGitFlowReporter reporter = JGitFlowReporter.get();
        if (files.size() > 0)
        {
            reporter.debugText(cmdName, reason + " details: ");

            for (String file : files)
            {
                reporter.debugText(cmdName, " -- " + reason + ": " + file);
            }
        }
    }

    /**
     * Tests to see if a tag exists with the given name
     *
     * @param git     The git instance to use
     * @param tagName The name of the tag to test for
     * @return if the tag exists or not
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static boolean tagExists(Git git, final String tagName) throws JGitFlowGitAPIException
    {
        boolean exists = false;

        if (StringUtils.isEmptyOrNull(tagName))
        {
            return exists;
        }

        try
        {
            List<Ref> refs = git.tagList().call();
            for (Ref ref : refs)
            {
                String simpleName = ref.getName().substring(ref.getName().indexOf(Constants.R_TAGS) + Constants.R_TAGS.length());
                if (simpleName.equals(tagName))
                {
                    exists = true;
                    break;
                }
            }

            return exists;
        }
        catch (GitAPIException e)
        {
            throw new JGitFlowGitAPIException(e);
        }
    }

    private static String getName()
    {
        return GitHelper.class.getSimpleName();
    }

}
