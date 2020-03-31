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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class HotfixStartTest extends BaseGitFlowTest
{
    @Test
    public void startHotfix() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.hotfixStart("1.0").call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());
    }

    @Test
    public void startHotfixWithFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("master").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        //update local
        git.checkout().setName("master").call();
        git.pull().call();
        git.checkout().setName("develop").call();

        flow.hotfixStart("1.0").setFetch(true).call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());
    }

    @Test
    public void startHotfixWithPush() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        flow.hotfixStart("1.0").setFetch(true).setPush(true).call();

        assertTrue(GitHelper.remoteBranchExists(git, "hotfix/1.0"));

    }

    @Test(expected = BranchOutOfDateException.class)
    public void startHotfixWithFetchLocalBehindMaster() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("master").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("master").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.hotfixStart("1.0").setFetch(true).call();

    }

    @Test
    public void startHotfixWithFetchLocalBehindMasterNoFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("master").call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("master").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.hotfixStart("1.0").call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());

    }

    public void startHotfixWithFetchNoRemote() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.hotfixStart("1.0").setFetch(true).call();

    }

    @Test(expected = NotInitializedException.class)
    public void startHotfixWithoutFlowInit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlow flow = JGitFlow.get(git.getRepository().getWorkTree());

        flow.hotfixStart("1.0").call();
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startHotfixWithUnStagedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //create a new file
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");

        flow.hotfixStart("1.0").call();
    }

    @Test(expected = DirtyWorkingTreeException.class)
    public void startHotfixUnCommittedFile() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //create a new file and add it to the index
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();

        flow.hotfixStart("1.0").call();
    }

    @Test
    public void startHotfixWithNewCommit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //we should be on develop branch
        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        //switch to master
        git.checkout().setName("master").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        //make sure master has our commit
        assertTrue(GitHelper.isMergedInto(git, commit, flow.getMasterBranchName()));

        flow.hotfixStart("1.0").call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());

        //the hotfix branch should have our commit
        assertTrue("hotfix branch does not have our commit: " + commit.toString(), GitHelper.isMergedInto(git, commit, flow.getHotfixBranchPrefix() + "1.0"));

    }

    @Test(expected = HotfixBranchExistsException.class)
    public void startHotfixWithExistingLocalBranch() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.branchCreate().setName(flow.getHotfixBranchPrefix() + "1.0").call();

        flow.hotfixStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startHotfixWithExistingLocalTag() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.tag().setName(flow.getVersionTagPrefix() + "1.0").call();

        flow.hotfixStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startHotfixWithExistingLocalPrefixedTag() throws Exception
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

        flow.hotfixStart("1.0").call();
    }

    @Test(expected = TagExistsException.class)
    public void startHotfixWithExistingRemoteTagAndFetch() throws Exception
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

        flow.hotfixStart("1.0").setFetch(true).call();

    }

    @Test
    public void startHotfixWithExistingRemoteTagNoFetch() throws Exception
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

        flow.hotfixStart("1.0").call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());

    }
}
