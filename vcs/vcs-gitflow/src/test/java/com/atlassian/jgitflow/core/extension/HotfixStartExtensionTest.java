package com.atlassian.jgitflow.core.extension;

import java.io.File;

import com.atlassian.jgitflow.core.BaseGitFlowTest;
import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.BaseExtensionForTests;
import com.atlassian.jgitflow.core.testutils.HotfixStartExtensionForTests;
import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.*;

public class HotfixStartExtensionTest extends BaseGitFlowTest
{
    @Test
    public void startHotfixExtension() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        HotfixStartExtensionForTests extension = new HotfixStartExtensionForTests();

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

        flow.hotfixStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));
    }

    @Test
    public void startHotfixExtensionWithThrownException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        HotfixStartExtensionForTests extension = new HotfixStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.ERROR);

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

        try
        {
            flow.hotfixStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

            fail("Exception should have been thrown!!");
        }
        catch (JGitFlowExtensionException e)
        {
            assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
            assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
            assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
            assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        }
    }

    @Test
    public void startHotfixExtensionWithWarnException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        HotfixStartExtensionForTests extension = new HotfixStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.WARN);

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

        flow.hotfixStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertEquals(flow.getHotfixBranchPrefix() + "1.0", git.getRepository().getBranch());

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));
    }
}
