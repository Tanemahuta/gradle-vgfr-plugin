package com.atlassian.jgitflow.core;

import java.io.File;
import java.io.IOException;

import com.atlassian.jgitflow.core.command.*;
import com.atlassian.jgitflow.core.exception.AlreadyInitializedException;
import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.exception.JGitFlowIOException;
import com.atlassian.jgitflow.core.exception.SameBranchException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.RepositoryBuilder;

/**
 * Offers a Git Flow API to interact with a git repository.
 * <p></p>
 * This class only offers methods to construct so-called command classes. Each
 * command is represented by one command class.<br>
 * Example: this class offers a {@code featureStart} method returning an instance of
 * the {@code FeatureStartCommand} class. The {@code FeatureStartCommand} class has setters
 * for all the arguments and options. The {@code FeatureStartCommand} class also has a
 * {@code call} method to actually execute the command.
 * <p></p>
 * All mandatory parameters for commands have to be specified in the methods of
 * this class, the optional parameters have to be specified by the
 * setter-methods of the Command class.
 */
public class JGitFlow
{
    private Git git;
    private GitFlowConfiguration gfConfig;
    private JGitFlowReporter reporter = JGitFlowReporter.get();

    private JGitFlow()
    {
    }

    JGitFlow(Git git, GitFlowConfiguration gfConfig)
    {
        this.git = git;
        this.gfConfig = gfConfig;

        this.reporter.setGitFlowConfiguration(git, gfConfig);
    }

    /**
     * Initializes a project for use with git flow and returns a JGitFlow instance.
     * <p>
     * This method will throw exceptions if the project has already been initialized
     * </p>
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow init(File projectDir) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).call();
    }

    /**
     * Initializes a project for use with git flow and returns a JGitFlow instance.
     * <p>
     * This method will throw exceptions if the project has already been initialized
     * </p>
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow init(File projectDir, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setDefaultOriginUrl(defaultOriginUrl).call();
    }

    /**
     * Initializes a project for use with git flow using a custom context and returns a JGitFlow instance.
     * <p>
     * This method will throw exceptions if the project has already been initialized
     * </p>
     *
     * @param projectDir
     * @param context
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow init(File projectDir, InitContext context) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setInitContext(context).call();
    }

    /**
     * Initializes a project for use with git flow using a custom context and returns a JGitFlow instance.
     * <p>
     * This method will throw exceptions if the project has already been initialized
     * </p>
     *
     * @param projectDir
     * @param context
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow init(File projectDir, InitContext context, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setDefaultOriginUrl(defaultOriginUrl).setInitContext(context).call();
    }

    /**
     * Initializes a project for use with git flow using a default context overriding any existing configuration.
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow forceInit(File projectDir) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setForce(true).call();
    }

    /**
     * Initializes a project for use with git flow using a default context overriding any existing configuration.
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow forceInit(File projectDir, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setDefaultOriginUrl(defaultOriginUrl).setForce(true).call();
    }

    /**
     * Initializes a project for use with git flow using a custom context overriding any existing configuration.
     *
     * @param projectDir
     * @param context
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow forceInit(File projectDir, InitContext context) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setForce(true).setInitContext(context).call();
    }

    /**
     * Initializes a project for use with git flow using a custom context overriding any existing configuration.
     *
     * @param projectDir
     * @param context
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow forceInit(File projectDir, InitContext context, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setDefaultOriginUrl(defaultOriginUrl).setForce(true).setInitContext(context).call();
    }

    /**
     * Initializes a project for use with git flow using a custom context overriding any existing configuration.
     *
     * @param projectDir
     * @param context
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlowInitCommand forceInitCommand(File projectDir, InitContext context) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        JGitFlowInitCommand initCommand = new JGitFlowInitCommand();
        return initCommand.setDirectory(projectDir).setForce(true).setInitContext(context);
    }

    /**
     * Gets an existing git flow project and returns a JGitFlow instance
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    public static JGitFlow get(File projectDir) throws JGitFlowIOException
    {
        try
        {
            RepositoryBuilder rb = new RepositoryBuilder()
                    .readEnvironment()
                    .findGitDir(projectDir);

            File gitDir = rb.getGitDir();
            Git gitRepo = Git.open(gitDir);
            GitFlowConfiguration gfConfig = new GitFlowConfiguration(gitRepo);

            return new JGitFlow(gitRepo, gfConfig);
        }
        catch (IOException e)
        {
            throw new JGitFlowIOException(e);
        }
    }

    /**
     * Initializes a project for use with git flow or gets an existing project and returns a JGitFlow instance.
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow getOrInit(File projectDir) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        if (isInitialized(projectDir))
        {
            return get(projectDir);
        }
        else
        {
            return init(projectDir);
        }
    }

    /**
     * Initializes a project for use with git flow or gets an existing project and returns a JGitFlow instance.
     *
     * @param projectDir
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow getOrInit(File projectDir, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        if (isInitialized(projectDir))
        {
            return get(projectDir);
        }
        else
        {
            return init(projectDir, defaultOriginUrl);
        }
    }

    /**
     * Initializes a project for use with git flow using a custom context or gets an existing project and returns a JGitFlow instance.
     *
     * @param projectDir
     * @param ctx
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow getOrInit(File projectDir, InitContext ctx) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        if (isInitialized(projectDir))
        {
            return get(projectDir);
        }
        else
        {
            return init(projectDir, ctx);
        }
    }

    /**
     * Initializes a project for use with git flow using a custom context or gets an existing project and returns a JGitFlow instance.
     *
     * @param projectDir
     * @param ctx
     * @return
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     * @throws com.atlassian.jgitflow.core.exception.AlreadyInitializedException
     * @throws com.atlassian.jgitflow.core.exception.SameBranchException
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException
     */
    public static JGitFlow getOrInit(File projectDir, InitContext ctx, String defaultOriginUrl) throws JGitFlowIOException, AlreadyInitializedException, SameBranchException, JGitFlowGitAPIException
    {
        if (isInitialized(projectDir))
        {
            return get(projectDir);
        }
        else
        {
            return init(projectDir, ctx, defaultOriginUrl);
        }
    }


    /**
     * Tests whether a project folder is git flow enabled
     *
     * @param dir
     * @return
     */
    public static boolean isInitialized(File dir)
    {
        boolean inited = false;
        try
        {
            RepositoryBuilder rb = new RepositoryBuilder()
                    .readEnvironment()
                    .findGitDir(dir);

            File gitDir = rb.getGitDir();
            if (gitDir != null)
            {
                Git gitRepo = Git.open(gitDir);
                GitFlowConfiguration gfConfig = new GitFlowConfiguration(gitRepo);
                inited = gfConfig.gitFlowIsInitialized();
            }

        }
        catch (IOException e)
        {
            //ignore
        }
        catch (JGitFlowGitAPIException e)
        {
            //ignore
        }

        return inited;
    }

    /**
     * Tests whether the current project is git flow enabled
     *
     * @return
     */
    public boolean isInitialized()
    {
        boolean inited = false;
        try
        {
            inited = gfConfig.gitFlowIsInitialized();
        }
        catch (JGitFlowGitAPIException e)
        {
            //ignore
        }

        return inited;
    }

    /**
     * Returns a command object to start a feature
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.FeatureStartCommand}
     */
    public FeatureStartCommand featureStart(String name)
    {
        return new FeatureStartCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to finish a feature
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.FeatureFinishCommand}
     */
    public FeatureFinishCommand featureFinish(String name)
    {
        return new FeatureFinishCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to publish a feature
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.FeaturePublishCommand}
     */
    public FeaturePublishCommand featurePublish(String name)
    {
        return new FeaturePublishCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to start a release
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.ReleaseStartCommand}
     */
    public ReleaseStartCommand releaseStart(String name)
    {
        return new ReleaseStartCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to finish a release
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.ReleaseFinishCommand}
     */
    public ReleaseFinishCommand releaseFinish(String name)
    {
        return new ReleaseFinishCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to publish a release
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.ReleasePublishCommand}
     */
    public ReleasePublishCommand releasePublish(String name)
    {
        return new ReleasePublishCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to start a hotfix
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.HotfixStartCommand}
     */
    public HotfixStartCommand hotfixStart(String name)
    {
        return new HotfixStartCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to finish a hotfix
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.HotfixFinishCommand}
     */
    public HotfixFinishCommand hotfixFinish(String name)
    {
        return new HotfixFinishCommand(name, git, gfConfig);
    }

    /**
     * Returns a command object to publish a hotfix
     *
     * @param name
     * @return a {@link com.atlassian.jgitflow.core.command.HotfixPublishCommand}
     */
    public HotfixPublishCommand hotfixPublish(String name)
    {
        return new HotfixPublishCommand(name, git, gfConfig);
    }

    /**
     * Returns the {@link org.eclipse.jgit.api.Git} instance used by this JGitFlow instance
     *
     * @return
     */
    public Git git()
    {
        return git;
    }

    /**
     * Returns the master branch name configured for this instance's git flow project
     *
     * @return
     */
    public String getMasterBranchName()
    {
        return gfConfig.getMaster();
    }

    /**
     * Returns the develop branch name configured for this instance's git flow project
     *
     * @return
     */
    public String getDevelopBranchName()
    {
        return gfConfig.getDevelop();
    }

    /**
     * Returns the feature branch prefix configured for this instance's git flow project
     *
     * @return
     */
    public String getFeatureBranchPrefix()
    {
        return gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.FEATURE.configKey());
    }

    /**
     * Returns the release branch prefix configured for this instance's git flow project
     *
     * @return
     */
    public String getReleaseBranchPrefix()
    {
        return gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.RELEASE.configKey());
    }

    /**
     * Returns the hotfix branch prefix configured for this instance's git flow project
     *
     * @return
     */
    public String getHotfixBranchPrefix()
    {
        return gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.HOTFIX.configKey());
    }

    public String getSupportBranchPrefix()
    {
        return gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.SUPPORT.configKey());
    }

    /**
     * Returns the versiontag prefix configured for this instance's git flow project
     *
     * @return
     */
    public String getVersionTagPrefix()
    {
        return gfConfig.getPrefixValue(JGitFlowConstants.PREFIXES.VERSIONTAG.configKey());
    }

    public String getPrefixForBranch(String branchName)
    {
        return gfConfig.getPrefixForBranch(branchName);
    }

    public BranchType getTypeForBranch(String branchName)
    {
        return gfConfig.getTypeForBranch(branchName);
    }

}
