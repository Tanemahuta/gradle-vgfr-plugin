package com.atlassian.jgitflow.core;

import org.eclipse.jgit.util.StringUtils;

import static com.atlassian.jgitflow.core.util.Preconditions.checkNotNull;
import static com.atlassian.jgitflow.core.util.Preconditions.checkState;

/**
 * Initialization context to be used when initializing a git flow project.
 * <p>
 * Instances of this class can be passed to the {@link com.atlassian.jgitflow.core.JGitFlow} init methods to override the default
 * branch names and prefixes used by git flow.
 * </p>
 * <p></p>
 * Examples:
 * <p></p>
 * Override master branch name
 * <p></p>
 * <pre>
 * InitContext ctx = new InitContext();
 * ctx.setMaster("GA");
 *
 * JGitFlow flow = JGitFlow.getOrInit(new File(&quot;some dir&quot;), ctx);
 * </pre>
 * <p></p>
 * Override master branch and release prefix
 * <p></p>
 * <pre>
 * InitContext ctx = new InitContext();
 * ctx.setMaster(&quot;GA&quot;).setRelease(&quot;rel/&quot;);
 *
 * JGitFlow flow = JGitFlow.getOrInit(new File(&quot;some dir&quot;), ctx);
 * </pre>
 */
public class InitContext
{
    private String master;
    private String develop;
    private String feature;
    private String release;
    private String hotfix;
    private String support;
    private String versiontag;


    /**
     * Create a new initi context with the default git flow branches and prefixes
     */
    public InitContext()
    {
        this.master = "master";
        this.develop = "develop";
        this.feature = "feature/";
        this.release = "release/";
        this.hotfix = "hotfix/";
        this.support = "support/";
        this.versiontag = "";
    }

    /**
     * Set the name of the master branch
     *
     * @param master
     * @return {@code this}
     */
    public InitContext setMaster(String master)
    {
        checkState(!StringUtils.isEmptyOrNull(master));

        this.master = master;
        return this;
    }

    /**
     * Set the name of the develop branch
     *
     * @param develop
     * @return {@code this}
     */
    public InitContext setDevelop(String develop)
    {
        checkState(!StringUtils.isEmptyOrNull(develop));
        this.develop = develop;
        return this;
    }

    /**
     * Set the feature branch prefix
     *
     * @param feature
     * @return {@code this}
     */
    public InitContext setFeature(String feature)
    {
        checkState(!StringUtils.isEmptyOrNull(feature));
        this.feature = feature;
        return this;
    }

    /**
     * Set the release branch prefix
     *
     * @param release
     * @return {@code this}
     */
    public InitContext setRelease(String release)
    {
        checkState(!StringUtils.isEmptyOrNull(release));
        this.release = release;
        return this;
    }

    /**
     * Set the hotfix branch prefix
     *
     * @param hotfix
     * @return {@code this}
     */
    public InitContext setHotfix(String hotfix)
    {
        checkState(!StringUtils.isEmptyOrNull(hotfix));
        this.hotfix = hotfix;
        return this;
    }

    public InitContext setSupport(String support)
    {
        checkState(!StringUtils.isEmptyOrNull(support));
        this.support = support;
        return this;
    }

    /**
     * Set the prefix used when creating tags
     *
     * @param versiontag
     * @return {@code this}
     */
    public InitContext setVersiontag(String versiontag)
    {
        checkNotNull(versiontag);

        this.versiontag = versiontag;
        return this;
    }

    public String getMaster()
    {
        return master;
    }

    public String getDevelop()
    {
        return develop;
    }

    public String getFeature()
    {
        return feature;
    }

    public String getRelease()
    {
        return release;
    }

    public String getHotfix()
    {
        return hotfix;
    }

    public String getSupport()
    {
        return support;
    }

    public String getVersiontag()
    {
        return versiontag;
    }

    public void setPrefix(String prefixName, String prefixValue)
    {
        JGitFlowConstants.PREFIXES prefix = JGitFlowConstants.PREFIXES.valueOf(prefixName.toUpperCase());
        switch (prefix)
        {
            case FEATURE:
                setFeature(prefixValue);
                break;
            case RELEASE:
                setRelease(prefixValue);
                break;
            case HOTFIX:
                setHotfix(prefixValue);
                break;
            case SUPPORT:
                setSupport(prefixValue);
                break;
            case VERSIONTAG:
                setVersiontag(prefixValue);
                break;
        }
    }

    public String getPrefix(String prefixName)
    {
        JGitFlowConstants.PREFIXES prefix = JGitFlowConstants.PREFIXES.valueOf(prefixName.toUpperCase());
        String value = null;
        switch (prefix)
        {
            case FEATURE:
                value = getFeature();
                break;
            case RELEASE:
                value = getRelease();
                break;
            case HOTFIX:
                value = getHotfix();
                break;
            case SUPPORT:
                value = getSupport();
                break;
            case VERSIONTAG:
                value = getVersiontag();
                break;
        }

        return value;
    }
}