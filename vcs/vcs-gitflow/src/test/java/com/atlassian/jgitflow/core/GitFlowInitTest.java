package com.atlassian.jgitflow.core;

import java.io.File;

import com.atlassian.jgitflow.core.InitContext;
import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.AlreadyInitializedException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.util.IO;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitFlowInitTest extends BaseGitFlowTest
{
    @Test
    public void initInCleanRepo() throws Exception
    {
        File workDir = newDir();
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(workDir).call();

        flow.git().checkout().setName("develop").call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = new File(workDir, ".git");
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));

        assertEquals("master", flow.getMasterBranchName());
        assertEquals("develop", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());
    }

    @Test
    public void initInCleanRepoWithContext() throws Exception
    {
        File workDir = newDir();
        InitContext ctx = new InitContext();
        ctx.setMaster("own").setDevelop("you");

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(workDir).setInitContext(ctx).call();

        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = new File(workDir, ".git");
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));

        assertEquals("own", flow.getMasterBranchName());
        assertEquals("you", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());
    }

    @Test
    public void initInExistingRepo() throws Exception
    {
        Git git = null;
        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.git().checkout().setName("develop").call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));

        assertEquals("master", flow.getMasterBranchName());
        assertEquals("develop", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInExistingRepoWithContext() throws Exception
    {
        Git git = null;
        InitContext ctx = new InitContext();
        ctx.setMaster("own").setDevelop("you");

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).setInitContext(ctx).call();

        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));


        assertEquals("own", flow.getMasterBranchName());
        assertEquals("you", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInExistingRepoWithRemote() throws Exception
    {
        Git gfGit = null;
        Git remoteGit = null;

        File workDir = newDir();
        remoteGit = RepoUtil.createRepositoryWithMaster(newDir());
        gfGit = Git.cloneRepository().setDirectory(workDir).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(gfGit.getRepository().getWorkTree()).call();

        flow.git().checkout().setName("develop").call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = gfGit.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));


        assertEquals("master", flow.getMasterBranchName());
        assertEquals("develop", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInExistingRepoWithRemoteAndContext() throws Exception
    {
        Git gfGit = null;
        Git remoteGit = null;

        File workDir = newDir();
        InitContext ctx = new InitContext();
        ctx.setMaster("own").setDevelop("you");

        remoteGit = RepoUtil.createRepositoryWithMaster(newDir());
        gfGit = Git.cloneRepository().setDirectory(workDir).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(gfGit.getRepository().getWorkTree()).setInitContext(ctx).call();

        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = gfGit.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));


        assertEquals("own", flow.getMasterBranchName());
        assertEquals("you", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInExistingRepoWithCustomRemoteAndContext() throws Exception
    {
        Git gfGit = null;
        Git remoteGit = null;

        File workDir = newDir();
        InitContext ctx = new InitContext();
        ctx.setMaster("own").setDevelop("you");

        remoteGit = RepoUtil.createRepositoryWithBranches(newDir(), "own");
        gfGit = Git.cloneRepository().setDirectory(workDir).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(gfGit.getRepository().getWorkTree()).setInitContext(ctx).call();

        flow.git().checkout().setName(flow.getDevelopBranchName()).call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = gfGit.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));


        assertEquals("own", flow.getMasterBranchName());
        assertEquals("you", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test(expected = AlreadyInitializedException.class)
    public void initInExistingFlowRepo() throws Exception
    {
        Git git = null;
        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        //try to re-init
        JGitFlowInitCommand initCommand2 = new JGitFlowInitCommand();

        //this should throw
        initCommand2.setDirectory(git.getRepository().getWorkTree()).call();

    }

    @Test
    public void initInExistingFlowRepoWithForce() throws Exception
    {
        Git git = null;

        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        //try to re-init
        JGitFlowInitCommand initCommand2 = new JGitFlowInitCommand();

        //this should NOT throw
        initCommand2.setDirectory(git.getRepository().getWorkTree()).setForce(true).call();

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));


        assertEquals("master", flow.getMasterBranchName());
        assertEquals("develop", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInExistingFlowRepoWithForceAndContext() throws Exception
    {
        Git git = null;

        InitContext ctx = new InitContext();
        ctx.setMaster("own").setDevelop("you");
        git = RepoUtil.createRepositoryWithMaster(newDir());
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        //try to re-init
        JGitFlowInitCommand initCommand2 = new JGitFlowInitCommand();

        //this should NOT throw
        JGitFlow flow2 = initCommand2.setDirectory(git.getRepository().getWorkTree()).setForce(true).setInitContext(ctx).call();

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));

        //here we check the new instance
        assertEquals("own", flow2.getMasterBranchName());

        //now let's make sure the old instance also sees the config changes
        assertEquals("you", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initInNestedFolder() throws Exception
    {
        Git git = null;
        git = RepoUtil.createRepositoryWithMaster(newDir());

        File root = git.getRepository().getWorkTree();
        File nested1 = new File(root, "nested1");
        File nested2 = new File(nested1, "nested2");

        nested2.mkdirs();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(nested2).call();

        flow.git().checkout().setName("develop").call();
        assertEquals(flow.getDevelopBranchName(), flow.git().getRepository().getBranch());

        File gitDir = git.getRepository().getDirectory();
        File gitConfig = new File(gitDir, "config");

        assertTrue(gitConfig.exists());

        String configText = new String(IO.readFully(gitConfig));

        assertTrue(configText.contains("[gitflow \"branch\"]"));
        assertTrue(configText.contains("[gitflow \"prefix\"]"));

        assertEquals("master", flow.getMasterBranchName());
        assertEquals("develop", flow.getDevelopBranchName());
        assertEquals("feature/", flow.getFeatureBranchPrefix());
        assertEquals("release/", flow.getReleaseBranchPrefix());
        assertEquals("hotfix/", flow.getHotfixBranchPrefix());
        assertEquals("support/", flow.getSupportBranchPrefix());
        assertEquals("", flow.getVersionTagPrefix());

    }

    @Test
    public void initWithNoMasterReturnsToCurrentBranch() throws Exception
    {
        String featureBranch = "feature/my-feature";

        Git git = null;
        Git remoteGit = null;

        File workDir = newDir();

        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        remoteGit.checkout().setName("develop").call();
        remoteGit.add().addFilepattern(".").call();
        remoteGit.commit().setMessage("initial commit").call();

        remoteGit.branchDelete().setBranchNames("master").setForce(true).call();

        remoteGit.branchCreate().setName(featureBranch).call();

        git = Git.cloneRepository().setDirectory(workDir).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).setBranch(featureBranch).setCloneAllBranches(false).call();

        assertEquals(featureBranch, git.getRepository().getBranch());

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        //make sure we're STILL on the feature
        assertEquals(featureBranch, git.getRepository().getBranch());

    }
}
