package com.atlassian.jgitflow.core;

import org.eclipse.jgit.api.Git;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * @since version
 */
public class ReportTest
{
    @Test
    public void jgitVersion() throws Exception
    {
        Package pkg = Git.class.getPackage();
        String version = pkg.getImplementationVersion();

        assertNotNull(version);
        System.out.println("git version = " + version);

    }
}
