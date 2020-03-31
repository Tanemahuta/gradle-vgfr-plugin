package com.atlassian.jgitflow.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.exception.JGitFlowIOException;
import com.atlassian.jgitflow.core.util.GitHelper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.StoredConfig;

/**
 * Represents the configuration for a git flow project
 * <p>
 * Instances of this class are usually created internally by initializing a git flow project.
 * </p>
 * <p>
 * for example: JGitFlow.getOrInit(new File(&quot;some dir&quot;));
 * </p>
 */
public class GitFlowConfiguration
{
    public static final List<String> PREFIX_NAMES = Arrays.asList(
            JGitFlowConstants.PREFIXES.FEATURE.configKey()
            , JGitFlowConstants.PREFIXES.RELEASE.configKey()
            , JGitFlowConstants.PREFIXES.HOTFIX.configKey()
            , JGitFlowConstants.PREFIXES.SUPPORT.configKey()
            , JGitFlowConstants.PREFIXES.VERSIONTAG.configKey());

    private final Git git;

    /**
     * Create a new configuration instance
     *
     * @param git The git instance to use
     */
    public GitFlowConfiguration(Git git)
    {
        this.git = git;
    }

    /**
     * @return The name of the develop branch
     */
    public String getDevelop()
    {
        return git.getRepository().getConfig().getString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, JGitFlowConstants.DEVELOP_KEY);
    }

    /**
     * @return the name of the master branch
     */
    public String getMaster()
    {
        return git.getRepository().getConfig().getString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, Constants.MASTER);
    }

    /**
     * Sets the name of the master branch
     *
     * @param branchName
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public void setMaster(String branchName) throws JGitFlowIOException
    {
        StoredConfig config = git.getRepository().getConfig();
        config.setString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, Constants.MASTER, branchName);
        try
        {
            config.save();
            config.load();
        }
        catch (Exception e)
        {
            throw new JGitFlowIOException(e);
        }
    }

    /**
     * Sets the name of the develop branch
     *
     * @param branchName
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public void setDevelop(String branchName) throws JGitFlowIOException
    {
        StoredConfig config = git.getRepository().getConfig();
        config.setString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, JGitFlowConstants.DEVELOP_KEY, branchName);
        try
        {
            config.save();
            config.load();
        }
        catch (Exception e)
        {
            throw new JGitFlowIOException(e);
        }
    }

    /**
     * @return if the current project has been initialized for git flow
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public boolean gitFlowIsInitialized() throws JGitFlowGitAPIException
    {
        return (hasMasterConfigured() && hasDevelopConfigured() && !getMaster().equals(getDevelop()) && hasPrefixesConfigured());
    }

    /**
     * @return if the local develop branch exists
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public boolean hasDevelopConfigured() throws JGitFlowGitAPIException
    {
        return GitHelper.localBranchExists(git, git.getRepository().getConfig().getString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, JGitFlowConstants.DEVELOP_KEY));
    }

    /**
     * @return if the local master branch exists
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public boolean hasMasterConfigured() throws JGitFlowGitAPIException
    {
        return GitHelper.localBranchExists(git, git.getRepository().getConfig().getString(JGitFlowConstants.SECTION, ConfigConstants.CONFIG_BRANCH_SECTION, Constants.MASTER));
    }

    /**
     * @return if all of the prefixes have been configured
     */
    public boolean hasPrefixesConfigured()
    {
        Set<String> entries = git.getRepository().getConfig().getNames(JGitFlowConstants.SECTION, JGitFlowConstants.PREFIX_SUB);

        return entries.containsAll(getPrefixNames());
    }

    /**
     * @return A list of all the prefix names
     */
    public List<String> getPrefixNames()
    {
        return PREFIX_NAMES;
    }

    /**
     * @param prefixName
     * @return If a specific prefix has been configured
     */
    public boolean hasPrefixConfigured(String prefixName)
    {
        Set<String> entries = git.getRepository().getConfig().getNames(JGitFlowConstants.SECTION, JGitFlowConstants.PREFIX_SUB);

        return entries.contains(prefixName);

    }

    /**
     * @param prefixName
     * @return The configured value of the given prefix
     */
    public String getPrefixValue(String prefixName)
    {
        String val = git.getRepository().getConfig().getString(JGitFlowConstants.SECTION, JGitFlowConstants.PREFIX_SUB, prefixName);

        return (null != val) ? val : "";
    }

    /**
     * Sets the value for a given prefix
     *
     * @param prefixName
     * @param prefixValue
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public void setPrefix(String prefixName, String prefixValue) throws JGitFlowIOException
    {
        StoredConfig config = git.getRepository().getConfig();
        if (getPrefixNames().contains(prefixName))
        {
            config.setString(JGitFlowConstants.SECTION, JGitFlowConstants.PREFIX_SUB, prefixName, prefixValue);
            try
            {
                config.save();
                config.load();
            }
            catch (Exception e)
            {
                throw new JGitFlowIOException(e);
            }
        }

    }

    public String getPrefixForBranch(String branchName)
    {
        String branchPrefix = "";

        for (String prefixName : getPrefixNames())
        {
            if (hasPrefixConfigured(prefixName))
            {
                String prefix = getPrefixValue(prefixName);

                if (branchName.startsWith(prefix))
                {
                    branchPrefix = prefix;
                    break;
                }
            }
        }

        return branchPrefix;
    }

    public BranchType getTypeForBranch(String branchName)
    {
        if (getMaster().equals(branchName))
        {
            return BranchType.MASTER;
        }

        if (getDevelop().equals(branchName))
        {
            return BranchType.DEVELOP;
        }

        String branchPrefix = "";

        for (String prefixName : getPrefixNames())
        {
            if (hasPrefixConfigured(prefixName))
            {
                String prefix = getPrefixValue(prefixName);

                if (branchName.startsWith(prefix))
                {
                    try
                    {
                        return BranchType.valueOf(prefixName.toUpperCase());
                    }
                    catch (IllegalArgumentException e)
                    {
                        return BranchType.UNKNOWN;
                    }
                }
            }
        }

        return BranchType.UNKNOWN;
    }
}
