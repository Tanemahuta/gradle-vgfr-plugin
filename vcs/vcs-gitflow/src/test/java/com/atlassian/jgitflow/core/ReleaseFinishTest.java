package com.atlassian.jgitflow.core;

import java.io.File;
import java.util.List;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.ReleaseMergeResult;
import com.atlassian.jgitflow.core.exception.BranchOutOfDateException;
import com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.*;

/**
 * @since version
 */
public class ReleaseFinishTest extends BaseGitFlowTest
{
    @Test
    public void finishRelease() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

        ReleaseMergeResult result = flow.releaseFinish("1.0").call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNull(ref2check);

    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void finishReleaseWithUnStagedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new file
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").call();
        assertTrue(result.wasSuccessful());
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void finishReleaseUnCommittedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new file and add it to the index
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").call();
        assertTrue(result.wasSuccessful());
    }

    @Test
    public void finishReleaseWithNewCommit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop doesn't report our commit yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNull(ref2check);

        //the develop branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //since fast-forward is suppressed the latest commit on develop should be a merge commit with 2 parents
        assertEquals(2, GitHelper.getLatestCommit(git, flow.getDevelopBranchName()).getParentCount());

        //the master branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getMasterBranchName()));

        //since fast-forward is suppressed the latest commit on master should be a merge commit with 2 parents
        assertEquals(2, GitHelper.getLatestCommit(git, flow.getMasterBranchName()).getParentCount());
    }

    @Test
    public void finishReleaseWithNewCommitNoMerge() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop doesn't report our commit yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").setNoMerge(true).call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNull(ref2check);

        //the develop branch should have NOT our commit
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //the master branch should NOT have our commit
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getMasterBranchName()));
    }

    @Test
    public void finishReleaseKeepBranch() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //just in case
        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

        ReleaseMergeResult result = flow.releaseFinish("1.0").setKeepBranch(true).call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should still exist
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNotNull(ref2check);
    }

    @Test
    public void finishReleaseWithMultipleCommits() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //create second commit
        File junkFile2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junkFile2, "I am junk, and so are you");
        git.add().addFilepattern(junkFile2.getName()).call();
        RevCommit commit2 = git.commit().setMessage("updating junk file").call();

        //make sure develop doesn't have our commits yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertFalse(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNull(ref2check);

        //the develop branch should have both of our commits now
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertTrue(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));

        //the master branch should have both of our commits now
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getMasterBranchName()));
        assertTrue(GitHelper.isMergedInto(git, commit2, flow.getMasterBranchName()));
    }

    @Test
    public void finishReleaseWithSquash() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //create second commit
        File junkFile2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junkFile2, "I am junk, and so are you");
        git.add().addFilepattern(junkFile2.getName()).call();
        RevCommit commit2 = git.commit().setMessage("updating junk file").call();

        //make sure develop doesn't have our commits yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertFalse(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));

        //try to finish
        ReleaseMergeResult result = flow.releaseFinish("1.0").setSquash(true).call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "1.0");
        assertNull(ref2check);

        //the develop branch should NOT have both of our commits now
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertFalse(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));

        //the master branch should NOT have both of our commits now
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getMasterBranchName()));
        assertFalse(GitHelper.isMergedInto(git, commit2, flow.getMasterBranchName()));

        //we should have the release files
        git.checkout().setName(flow.getDevelopBranchName()).call();
        File developJunk = new File(git.getRepository().getWorkTree(), "junk.txt");
        File developJunk2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        assertTrue(developJunk.exists());
        assertTrue(developJunk2.exists());

        git.checkout().setName(flow.getMasterBranchName()).call();
        File masterJunk = new File(git.getRepository().getWorkTree(), "junk.txt");
        File masterJunk2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        assertTrue(masterJunk.exists());
        assertTrue(masterJunk2.exists());
    }

    @Test(expected = BranchOutOfDateException.class)
    public void finishReleaseDevelopBehindRemoteWithFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName(flow.getDevelopBranchName()).call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        ReleaseMergeResult result = flow.releaseFinish("1.0").setFetch(true).call();

        assertTrue(result.wasSuccessful());

    }

    @Test(expected = BranchOutOfDateException.class)
    public void finishReleaseMasterBehindRemoteWithFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        //do a commit to the remote master branch
        remoteGit.checkout().setName(flow.getMasterBranchName()).call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        ReleaseMergeResult result = flow.releaseFinish("1.0").setFetch(true).call();

        assertTrue(result.wasSuccessful());

    }

    //TODO: add tests for push and tag flags
    @Test
    public void finishReleaseWithRemoteReleaseAndPush() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        flow.git().push().setRemote("origin").call();

        //do a commit to the remote develop branch
        List<Ref> remoteBranches = remoteGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        boolean hasRemoteRelease = false;

        for (Ref remoteBranch : remoteBranches)
        {
            if (remoteBranch.getName().equals(Constants.R_HEADS + flow.getReleaseBranchPrefix() + "1.0"))
            {
                hasRemoteRelease = true;
                break;
            }
        }

        assertTrue(hasRemoteRelease);

        File junkFile = new File(flow.git().getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        flow.git().add().addFilepattern(junkFile.getName()).call();
        RevCommit localcommit = flow.git().commit().setMessage("adding junk file").call();

        ReleaseMergeResult result = flow.releaseFinish("1.0").setPush(true).call();

        assertTrue(result.wasSuccessful());

        assertTrue(GitHelper.isMergedInto(remoteGit, localcommit, flow.getMasterBranchName()));
        assertTrue(GitHelper.isMergedInto(remoteGit, localcommit, flow.getDevelopBranchName()));
        assertFalse(GitHelper.remoteBranchExists(git, flow.getReleaseBranchPrefix() + "1.0"));
        assertFalse(GitHelper.localBranchExists(remoteGit, flow.getReleaseBranchPrefix() + "1.0"));
    }

    @Test
    public void finishReleaseAfterHotfix() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //create a hotfix
        flow.hotfixStart("1.1").call();

        //create a release
        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        flow.releaseStart("2.0").call();

        //add a commit on hotfix
        flow.git().checkout().setName(flow.getHotfixBranchPrefix() + "1.1").call();
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //add a commit on release
        flow.git().checkout().setName(flow.getReleaseBranchPrefix() + "2.0").call();
        File junkFile2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junkFile2, "I am junk");
        git.add().addFilepattern(junkFile2.getName()).call();
        RevCommit commit2 = git.commit().setMessage("committing junk file").call();

        //finish the hotfix
        flow.git().checkout().setName(flow.getHotfixBranchPrefix() + "1.1").call();
        flow.hotfixFinish("1.1").call();

        //make sure release has the hotfix commit
        flow.git().checkout().setName(flow.getReleaseBranchPrefix() + "2.0").call();
        assertTrue(GitHelper.isMergedInto(flow.git(), commit, flow.getReleaseBranchPrefix() + "2.0"));
        
        //finish the release
        ReleaseMergeResult result = flow.releaseFinish("2.0").call();

        assertTrue(result.wasSuccessful());

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //release branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getReleaseBranchPrefix() + "2.0");
        assertNull(ref2check);

    }

}
