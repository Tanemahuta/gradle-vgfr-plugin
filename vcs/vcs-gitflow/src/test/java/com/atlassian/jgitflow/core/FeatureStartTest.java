package com.atlassian.jgitflow.core;

import java.io.File;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.BranchOutOfDateException;
import com.atlassian.jgitflow.core.exception.LocalBranchExistsException;
import com.atlassian.jgitflow.core.exception.NotInitializedException;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureStartTest extends BaseGitFlowTest
{
    @Test
    public void startFeature() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), git.getRepository().getBranch());

        flow.featureStart("my-feature").call();

        assertEquals(flow.getFeatureBranchPrefix() + "my-feature", git.getRepository().getBranch());

    }

    @Test
    public void startFeatureWithFetch() throws Exception
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
        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        git.pull().call();

        flow.featureStart("my-feature").setFetch(true).call();

    }

    @Test
    public void startFeatureWithPush() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        flow.featureStart("myFeature").setFetch(true).setPush(true).call();

        assertTrue(GitHelper.remoteBranchExists(git, "feature/myFeature"));

    }

    @Test(expected = BranchOutOfDateException.class)
    public void startFeatureWithFetchLocalBehindMaster() throws Exception
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

        flow.featureStart("my-feature").setFetch(true).call();

    }

    public void startFeatureWithFetchNoRemote() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.featureStart("my-feature").setFetch(true).call();
    }

    @Test(expected = LocalBranchExistsException.class)
    public void startFeatureWithExistingLocalBranch() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        git.branchCreate().setName(flow.getFeatureBranchPrefix() + "my-feature").call();

        flow.featureStart("my-feature").call();
    }

    @Test
    public void startFeatureWithLocalBehindMasterNoFetch() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        flow.featureStart("my-feature").call();

        assertEquals(flow.getFeatureBranchPrefix() + "my-feature", git.getRepository().getBranch());

    }

    @Test(expected = NotInitializedException.class)
    public void startFeatureWithoutFlowInit() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlow flow = JGitFlow.get(git.getRepository().getWorkTree());

        flow.featureStart("my-feature").call();
    }
}
