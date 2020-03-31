package com.atlassian.jgitflow.core;

import java.io.File;
import java.security.SecureRandom;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * @since version
 */
public abstract class BaseGitFlowTest
{
    protected File baseDir;
    private static final SecureRandom random = new SecureRandom();

    @Before
    public void setupBaseDir()
    {
        this.baseDir = newTempDir();
    }

    @After
    public void teardownBaseDir()
    {
        if (null != baseDir && baseDir.exists())
        {
            FileUtils.deleteQuietly(baseDir);
        }
    }

    public File newTempDir()
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String name = randomName("gftmp-");
        File tmp = new File(baseDir, name);

        tmp.mkdirs();

        return tmp;
    }

    public File newDir(String name)
    {
        return new File(baseDir, name);
    }

    public File newDir()
    {

        return newDir(randomName("gftest"));
    }

    private String randomName(String base)
    {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);

        return base + Long.toString(n);
    }
}
