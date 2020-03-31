package com.atlassian.jgitflow.core;

import java.io.File;
import java.util.List;

import com.atlassian.jgitflow.core.InitContext;
import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.*;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.*;

/**
 * @since version
 */
public class ReleaseStartTest extends BaseGitFlowTest
{
    @Test
    public void startRelease() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());
    }

    @Test
    public void startReleaseWithFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        //update local
        git.checkout().setName("develop").call();
        git.pull().call();

        flow.releaseStart("1.0").setFetch(true).call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());
    }

    @Test(expected = BranchOutOfDateException.class)
    public void startReleaseWithFetchLocalBehindMaster() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.releaseStart("1.0").setFetch(true).call();

    }

    @Test
    public void startReleaseWithPush() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        flow.releaseStart("1.0").setFetch(true).setPush(true).call();

        assertTrue(GitHelper.remoteBranchExists(git, "release/1.0"));

    }

    @Test
    public void startReleaseWithFetchLocalBehindMasterNoFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.releaseStart("1.0").call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

    }

    public void startReleaseWithFetchNoRemote() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.releaseStart("1.0").setFetch(true).call();

    }

    @Test(expected = NotInitializedException.class)
    public void startReleaseWithoutFlowInit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlow flow = JGitFlow.get(git.getRepository().getWorkTree());

        flow.releaseStart("1.0").call();
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseWithUnStagedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //create a new file
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");

        flow.releaseStart("1.0").call();
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseUnCommittedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //create a new file and add it to the index
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();

        flow.releaseStart("1.0").call();
    }

    @Test
    public void startReleaseWithNewCommit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        flow.releaseStart("1.0").call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

        //the release branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getReleaseBranchPrefix() + "1.0"));

    }

    @Test(expected = ReleaseBranchExistsException.class)
    public void startReleaseWithExistingLocalBranch() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.branchCreate().setName(flow.getReleaseBranchPrefix() + "1.0").call();

        flow.releaseStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startReleaseWithExistingLocalTag() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.tag().setName(flow.getVersionTagPrefix() + "1.0").call();

        flow.releaseStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startReleaseWithExistingLocalPrefixedTag() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        InitContext ctx = new InitContext();
        ctx.setVersiontag("vtag/");

        JGitFlow flow = initCommand.setInitContext(ctx).setDirectory(git.getRepository().getWorkTree()).call();

        git.tag().setName(flow.getVersionTagPrefix() + "1.0").call();

        //just to make sure
        List<Ref> refs = git.tagList().call();
        String name = refs.get(0).getName();

        assertEquals(Constants.R_TAGS + "vtag/1.0", name);

        flow.releaseStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startReleaseWithExistingRemoteTagAndFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //add the remote tag
        remoteGit.tag().setName(flow.getVersionTagPrefix() + "1.0").call();

        flow.releaseStart("1.0").setFetch(true).call();

    }

    @Test
    public void startReleaseWithExistingRemoteTagNoFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //add the remote tag
        remoteGit.tag().setName(flow.getVersionTagPrefix() + "1.0").call();

        flow.releaseStart("1.0").call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseWithUncommittedChange() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //change the file
        FileUtils.writeStringToFile(junkFile, "I am junk again");

        try
        {
            flow.releaseStart("1.0").call();
        }
        catch (DirtyWorkingTreeException e)
        {
            assertEquals("Working tree has uncommitted changes", e.getMessage());
            throw e;
        }


    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseWithUntrackedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //create a new file
        File junk2File = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junk2File, "I am junk 2");

        try
        {
            flow.releaseStart("1.0").call();
        }
        catch (DirtyWorkingTreeException e)
        {
            assertEquals("Working tree has untracked files", e.getMessage());
            throw e;
        }


    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseWithUncommittedAddedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //create a new file
        File junk2File = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junk2File, "I am junk 2");
        git.add().addFilepattern(junk2File.getName()).call();

        try
        {
            flow.releaseStart("1.0").call();
        }
        catch (DirtyWorkingTreeException e)
        {
            assertEquals("Working tree has uncommitted changes", e.getMessage());
            throw e;
        }


    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startReleaseWithChangesAndUntrackedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //change the file
        FileUtils.writeStringToFile(junkFile, "I am junk again");

        //create a new file
        File junk2File = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junk2File, "I am junk 2");

        try
        {
            flow.releaseStart("1.0").call();
        }
        catch (DirtyWorkingTreeException e)
        {
            assertEquals("Working tree has uncommitted changes and untracked files", e.getMessage());
            throw e;
        }


    }

    @Test
    public void startReleaseWithAllowUntrackedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getDevelopBranchName()));

        //create a new file
        File junk2File = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junk2File, "I am junk 2");

        flow.releaseStart("1.0").setAllowUntracked(true).call();

    }

    @Test
    public void startReleaseFromSpecificCommit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.checkout().setName("develop").call();
        //we should be on develop branch
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commitOne = git.commit().setMessage("committing junk file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commitOne, flow.getDevelopBranchName()));

        //create a new commit
        File junkFile2 = new File(git.getRepository().getWorkTree(), "junk2.txt");
        FileUtils.writeStringToFile(junkFile2, "I am junk too");
        git.add().addFilepattern(junkFile2.getName()).call();
        RevCommit commitTwo = git.commit().setMessage("committing junk 2 file").call();

        //make sure develop has our commit
        assertTrue(GitHelper.isMergedInto(git, commitTwo, flow.getDevelopBranchName()));

        flow.releaseStart("1.0").setStartCommit(commitOne.getName()).call();

        assertEquals(flow.getReleaseBranchPrefix() + "1.0", git.getRepository().getBranch());

        //the release branch should have our commit
        assertTrue(GitHelper.isMergedInto(git, commitOne, flow.getReleaseBranchPrefix() + "1.0"));

        assertFalse(GitHelper.isMergedInto(git, commitTwo, flow.getReleaseBranchPrefix() + "1.0"));

    }


}
