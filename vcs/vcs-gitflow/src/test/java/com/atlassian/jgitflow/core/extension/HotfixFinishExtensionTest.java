package com.atlassian.jgitflow.core.extension;

import java.io.File;
import java.util.List;

import com.atlassian.jgitflow.core.BaseGitFlowTest;
import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowInitCommand;
import com.atlassian.jgitflow.core.ReleaseMergeResult;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;
import com.atlassian.jgitflow.core.extension.ExtensionFailStrategy;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.BaseExtensionForTests;
import com.atlassian.jgitflow.core.testutils.HotfixFinishExtensionForTests;
import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HotfixFinishExtensionTest extends BaseGitFlowTest
{
    @Test
    public void finishHotfixExtension() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.hotfixStart("1.0").call();

        flow.git().push().setRemote("origin").call();

        //do a commit to the remote develop branch
        List<Ref> remoteBranches = remoteGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        boolean hasRemoteRelease = false;

        for (Ref remoteBranch : remoteBranches)
        {
            if (remoteBranch.getName().equals(Constants.R_HEADS + flow.getHotfixBranchPrefix() + "1.0"))
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

        HotfixFinishExtensionForTests extension = new HotfixFinishExtensionForTests();

        ReleaseMergeResult result = flow.hotfixFinish("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue(result.wasSuccessful());

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_CHECKOUT));
        assertTrue("afterMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_CHECKOUT));
        assertTrue("beforeMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_MERGE));
        assertTrue("afterMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_MERGE));
        assertTrue("beforeDevelopCheckout was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_DEVELOP_CHECKOUT));
        assertTrue("afterDevelopCheckout was not called", extension.wasCalled(BaseExtensionForTests.AFTER_DEVELOP_CHECKOUT));
        assertTrue("beforeDevelopMerge was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_DEVELOP_MERGE));
        assertTrue("afterDevelopMerge was not called", extension.wasCalled(BaseExtensionForTests.AFTER_DEVELOP_MERGE));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));

    }

    @Test
    public void finishHotfixExtensionWithThrownException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.hotfixStart("1.0").call();

        flow.git().push().setRemote("origin").call();

        //do a commit to the remote develop branch
        List<Ref> remoteBranches = remoteGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        boolean hasRemoteRelease = false;

        for (Ref remoteBranch : remoteBranches)
        {
            if (remoteBranch.getName().equals(Constants.R_HEADS + flow.getHotfixBranchPrefix() + "1.0"))
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

        HotfixFinishExtensionForTests extension = new HotfixFinishExtensionForTests();
        extension.withException(BaseExtensionForTests.BEFORE_DEVELOP_CHECKOUT, ExtensionFailStrategy.ERROR);

        try
        {
            ReleaseMergeResult result = flow.hotfixFinish("1.0").setFetch(true).setPush(true).setExtension(extension).call();

            fail("Exception should have been thrown!!");
        }
        catch (JGitFlowExtensionException e)
        {
            assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
            assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
            assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
            assertTrue("beforeMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_CHECKOUT));
            assertTrue("afterMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_CHECKOUT));
            assertTrue("beforeMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_MERGE));
            assertTrue("afterMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_MERGE));
        }

    }

    @Test
    public void finishHotfixExtensionWithWarnException() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());

        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        JGitFlow flow = initCommand.setDirectory(git.getRepository().getWorkTree()).call();

        flow.hotfixStart("1.0").call();

        flow.git().push().setRemote("origin").call();

        //do a commit to the remote develop branch
        List<Ref> remoteBranches = remoteGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        boolean hasRemoteRelease = false;

        for (Ref remoteBranch : remoteBranches)
        {
            if (remoteBranch.getName().equals(Constants.R_HEADS + flow.getHotfixBranchPrefix() + "1.0"))
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

        HotfixFinishExtensionForTests extension = new HotfixFinishExtensionForTests();
        extension.withException(BaseExtensionForTests.BEFORE_DEVELOP_CHECKOUT, ExtensionFailStrategy.WARN);

        ReleaseMergeResult result = flow.hotfixFinish("1.0").setFetch(true).setPush(true).setExtension(extension).call();

        assertTrue(result.wasSuccessful());

        assertTrue("before was not called", extension.wasCalled(BaseExtensionForTests.BEFORE));
        assertTrue("beforeFetch was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_FETCH));
        assertTrue("afterFetch was not called", extension.wasCalled(BaseExtensionForTests.AFTER_FETCH));
        assertTrue("beforeMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_CHECKOUT));
        assertTrue("afterMasterCheckout was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_CHECKOUT));
        assertTrue("beforeMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_MASTER_MERGE));
        assertTrue("afterMasterMerge was not called", extension.wasCalled(BaseExtensionForTests.AFTER_MASTER_MERGE));
        assertTrue("beforeDevelopCheckout was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_DEVELOP_CHECKOUT));
        assertTrue("afterDevelopCheckout was not called", extension.wasCalled(BaseExtensionForTests.AFTER_DEVELOP_CHECKOUT));
        assertTrue("beforeDevelopMerge was not called", extension.wasCalled(BaseExtensionForTests.BEFORE_DEVELOP_MERGE));
        assertTrue("afterDevelopMerge was not called", extension.wasCalled(BaseExtensionForTests.AFTER_DEVELOP_MERGE));
        assertTrue("afterPush was not called", extension.wasCalled(BaseExtensionForTests.AFTER_PUSH));
        assertTrue("after was not called", extension.wasCalled(BaseExtensionForTests.AFTER));

    }
}
