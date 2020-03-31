package com.atlassian.jgitflow.core.extension;

import com.atlassian.jgitflow.core.BaseGitFlowTest;
import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy;

import org.eclipse.jgit.api.Git;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.BaseExtensionForTests;
import com.atlassian.jgitflow.core.testutils.FeatureStartExtensionForTests;
import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FeatureStartExtensionTest extends BaseGitFlowTest
{
    @Test
    public void startFeatureExtension() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        FeatureStartExtensionForTests extension = new FeatureStartExtensionForTests();

        flow.featureStart("myFeature").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));
    }

    @Test
    public void startFeatureExtensionWithThrownException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        FeatureStartExtensionForTests extension = new FeatureStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.ERROR);

        try
        {
            flow.featureStart("myFeature").setFetch(true).setPush(true).setExtension(extension).call();

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
    public void startFeatureExtensionWithWarnException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();
        git.push().setRemote("origin").add("develop").call();

        FeatureStartExtensionForTests extension = new FeatureStartExtensionForTests();
        extension.withException(BaseExtensionForTests.AFTER_CREATE_BRANCH, ExtensionFailStrategy.WARN);

        flow.featureStart("myFeature").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_CREATE_BRANCH));
        assertTrue("afterCreateBranch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_CREATE_BRANCH));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));

    }

}
