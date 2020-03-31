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
import com.atlassian.jgitflow.core.testutils.ReleaseStartExtensionForTests;
import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReleaseStartExtensionTest extends BaseGitFlowTest
{
    @Test
    public void startReleaseExtension() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        ReleaseStartExtensionForTests extension = new ReleaseStartExtensionForTests();

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        //update local
        git.checkout().setName("develop").call();
        git.pull().call();

        flow.releaseStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));

    }

    @Test
    public void startReleaseExtensionWithWarnException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        ReleaseStartExtensionForTests extension = new ReleaseStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.WARN);

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        //update local
        git.checkout().setName("develop").call();
        git.pull().call();

        flow.releaseStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));

    }

    @Test
    public void startReleaseExtensionWithThrownException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        ReleaseStartExtensionForTests extension = new ReleaseStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.ERROR);

        //do a commit to the remote develop branch
        remoteGit.checkout().setName("develop").call();
        File junkFile = new File(remoteGit.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        remoteGit.add().addFilepattern(junkFile.getName()).call();
        remoteGit.commit().setMessage("adding junk file").call();

        //update local
        git.checkout().setName("develop").call();
        git.pull().call();

        try
        {
            flow.releaseStart("1.0").setFetch(true).setPush(true).setExtension(extension).call();

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
}
