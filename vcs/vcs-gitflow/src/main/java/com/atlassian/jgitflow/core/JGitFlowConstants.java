package com.atlassian.jgitflow.core;

import org.eclipse.jgit.lib.Constants;

/**
 * Constants used by JGitFlow
 */
public final class JGitFlowConstants
{
    public static final String SECTION = "gitflow";
    public static final String PREFIX_SUB = "prefix";
    public static final String DEVELOP_KEY = "develop";
    public static final String GITFLOW_DIR = ".gitflow";
    public static final String MERGE_BASE = "MERGE_BASE";
    public static final String R_REMOTE_ORIGIN = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/";

    public static enum PREFIXES
    {
        FEATURE, RELEASE, HOTFIX, SUPPORT, VERSIONTAG;

        public String configKey()
        {
            return name().toLowerCase();
        }
    }

}
