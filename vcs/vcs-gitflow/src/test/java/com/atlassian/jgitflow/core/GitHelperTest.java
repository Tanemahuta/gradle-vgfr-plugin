package com.atlassian.jgitflow.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jgitflow.core.exception.LocalBranchMissingException;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.atlassian.jgitflow.core.testutils.RepoUtil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class
        GitHelperTest extends BaseGitFlowTest
{
    @Test
    public void listBranchesWithPrefix() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        git.branchCreate().setName("feature/my-feature").call();
        git.branchCreate().setName("feature/my-feature2").call();
        git.branchCreate().setName("release/1.1").call();

        List<Ref> branches = GitHelper.listBranchesWithPrefix(git, "feature/");

        List<String> names = new ArrayList<String>();
        for (Ref ref : branches)
        {
            names.add(ref.getName());
        }

        assertTrue(names.contains(Constants.R_HEADS + "feature/my-feature"));
        assertTrue(names.contains(Constants.R_HEADS + "feature/my-feature2"));
        assertFalse(names.contains(Constants.R_HEADS + "release/1.1"));
    }

    @Test
    public void commitExistsOnMaster() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("master").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        assertTrue(GitHelper.isMergedInto(git, commit.getName(), "master"));
        assertFalse(GitHelper.isMergedInto(git, commit.getName(), "develop"));
    }

    @Test
    public void shortCommitNameExistsOnMaster() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("master").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        RevCommit commit = git.commit().setMessage("committing junk file").call();

        String shortCommit = commit.getName().substring(0, 6);
        assertTrue(GitHelper.isMergedInto(git, shortCommit, "master"));
        assertFalse(GitHelper.isMergedInto(git, shortCommit, "develop"));
    }

    @Test
    public void developExistsOnMaster() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("develop").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        assertFalse(GitHelper.isMergedInto(git, "develop", "master"));

        git.checkout().setName("master").call();
        git.merge().include(GitHelper.getLocalBranch(git, "develop")).call();

        assertTrue(GitHelper.isMergedInto(git, "develop", "master"));

    }

    @Test
    public void headExistsOnMaster() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("master").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        assertTrue(GitHelper.isMergedInto(git, "HEAD", "master"));

    }

    @Test
    public void developNotOnMaster() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("develop").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        assertFalse(GitHelper.isMergedInto(git, "develop", "master"));

    }

    @Test(expected = LocalBranchMissingException.class)
    public void unknownCommitThrows() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git.checkout().setName("master").call();

        //create a new commit
        File junkFile = new File(git.getRepository().getWorkTree(), "junk.txt");
        FileUtils.writeStringToFile(junkFile, "I am junk");
        git.add().addFilepattern(junkFile.getName()).call();
        git.commit().setMessage("committing junk file").call();

        assertFalse(GitHelper.isMergedInto(git, "junk", "master"));

    }

    @Test
    public void localTagExists() throws Exception
    {
        Git git = RepoUtil.createRepositoryWithMaster(newDir());
        git.tag().setName("1.0").setMessage("tagged 1.0").call();

        assertTrue(GitHelper.tagExists(git, "1.0"));
    }

    @Test
    public void remoteTagExists() throws Exception
    {
        Git git = null;
        Git remoteGit = null;
        remoteGit = RepoUtil.createRepositoryWithMasterAndDevelop(newDir());
        git = Git.cloneRepository().setDirectory(newDir()).setURI("file://" + remoteGit.getRepository().getWorkTree().getPath()).call();

        remoteGit.tag().setName("1.0").setMessage("tagged 1.0").call();

        git.fetch().call();

        assertTrue(GitHelper.tagExists(git, "1.0"));
    }
}
