package com.atlassian.jgitflow.core;

import java.io.File;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowConstants;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.BranchOutOfDateException;
import com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException;
import com.atlassian.jgitflow.core.exception.MergeConflictsNotResolvedException;
import com.atlassian.jgitflow.core.util.FileHelper;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.*;

/**
 * @since version
 */
public class FeatureFinishTest extends BaseGitFlowTest
{
    @Test
    public void finishFeature() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //just in case
        assertEquals(flow.getFeatureBranchPrefix() + "my-feature", git.getRepository().getBranch());

        flow.featureFinish("my-feature").call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNull(ref2check);

    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void finishFeatureWithUnStagedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //create a new file
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");

        //try to finish
        flow.featureFinish("my-feature").call();
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void finishFeatureUnCommittedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //create a new file and add it to the index
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();

        //try to finish
        flow.featureFinish("my-feature").call();
    }

    @Test
    public void finishFeatureWithNewCommit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop doesn't report our commit yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //try to finish
        flow.featureFinish("my-feature").call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNull(ref2check);

        //the develop branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //since fast-forward is not suppressed the latest commit should not be a merge commit
        assertEquals(1, GitHelper.getLatestCommit(git, flow.getDevelopBranchName()).getParentCount());
    }

    @Test
    public void finishFeatureWithNewCommitAndSuppressFastForward() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop doesn't report our commit yet
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //try to finish
        flow.featureFinish("my-feature").setSuppressFastForward(true).call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //the develop branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //since fast-forward is suppressed the latest commit should be a merge commit with 2 parents
        assertEquals(2, GitHelper.getLatestCommit(git, flow.getDevelopBranchName()).getParentCount());
    }

    @Test
    public void finishFeatureKeepBranch() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //just in case
        assertEquals(flow.getFeatureBranchPrefix() + "my-feature", git.getRepository().getBranch());

        flow.featureFinish("my-feature").setKeepBranch(true).call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should still exist
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNotNull(ref2check);
    }

    @Test
    public void finishFeatureWithMultipleCommits() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

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
        flow.featureFinish("my-feature").call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNull(ref2check);

        //the develop branch should have both of our commits now
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertTrue(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));
    }

    @Test
    public void finishFeatureWithSquash() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

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
        flow.featureFinish("my-feature").setSquash(true).call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNull(ref2check);

        //the develop branch should NOT have both of our commits now
        assertFalse(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));
        assertFalse(GitHelper.isMergedInto(git, commit2, flow.getDevelopBranchName()));

        //we should have the feature files
        File developJunk = new File(git.getRepository().getWorkTree(), "junk.txt");
        File developJunk2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        assertTrue(developJunk.exists());
        assertTrue(developJunk2.exists());
    }

    @Test(expected = BranchOutOfDateException.class)
    public void finishFeatureBehindRemoteWithFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithBranches(newDir(), "develop", "feature/my-feature");

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //do a commit to the remote feature branch
        remoteGit.checkout().setName(flow.getFeatureBranchPrefix() + "my-feature").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.featureFinish("my-feature").setFetch(true).call();

    }

    @Test
    public void finishFeatureWithRebase() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithBranches(newDir(), "develop", "feature/my-feature");

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();
        git.pull().call();

        //do a commit to the develop branch
        git.checkout().setName(flow.getDevelopBranchName()).call();
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("adding junk file").call();

        flow.featureFinish("my-feature").setRebase(true).call();

    }

    @Test(expected = MergeConflictsNotResolvedException.class)
    public void finishFeatureMergeConflict() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //go back to develop and do a commit
        git.checkout().setName(flow.getDevelopBranchName()).call();
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "A");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        //commit the same file in feature to create a conflict
        git.checkout().setName(flow.getFeatureBranchPrefix() + "my-feature").call();
        FileUtils.writeStringToFile(junkFile, "B");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        //try to finish
        try
        {
            flow.featureFinish("my-feature").setKeepBranch(true).call();
        }
        catch (Exception e)
        {
            File gitFlowDir = new File(git.getRepository().getDirectory(), JGitFlowConstants.GITFLOW_DIR);
            File mergeBase = new File(gitFlowDir, JGitFlowConstants.MERGE_BASE);
            assertTrue(mergeBase.exists());
            assertEquals(flow.getDevelopBranchName(), FileHelper.readFirstLine(mergeBase));

            throw e;
        }

    }

    @Test
    public void finishFeatureConflictRestore() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").call();

        //go back to develop and do a commit
        git.checkout().setName(flow.getDevelopBranchName()).call();
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "A");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        //commit the same file in feature to create a conflict
        git.checkout().setName(flow.getFeatureBranchPrefix() + "my-feature").call();
        FileUtils.writeStringToFile(junkFile, "B");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        boolean gotException = false;
        //try to finish
        try
        {
            flow.featureFinish("my-feature").call();
        }
        catch (Exception e)
        {
            gotException = true;
            File gitFlowDir = new File(git.getRepository().getDirectory(), JGitFlowConstants.GITFLOW_DIR);
            File mergeBase = new File(gitFlowDir, JGitFlowConstants.MERGE_BASE);
            assertTrue(mergeBase.exists());
            assertEquals(flow.getDevelopBranchName(), FileHelper.readFirstLine(mergeBase));
        }

        if (!gotException)
        {
            fail("Merge Conflict not detected!!");
        }

        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        FileUtils.writeStringToFile(junkFile, "A");
        git.add().addFilepattern(junkFile.getName()).setUpdate(true).call();
        git.commit().setMessage("merging").call();

        //try to finish again
        git.checkout().setName(flow.getFeatureBranchPrefix() + "my-feature").call();
        flow.featureFinish("my-feature").call();

        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //feature branch should be gone
        Ref ref2check = git.getRepository().findRef(flow.getFeatureBranchPrefix() + "my-feature");
        assertNull(ref2check);

        File gitFlowDir2 = new File(git.getRepository().getDirectory(), JGitFlowConstants.GITFLOW_DIR);
        File mergeBase2 = new File(gitFlowDir2, JGitFlowConstants.MERGE_BASE);
        assertFalse(mergeBase2.exists());

    }
}
