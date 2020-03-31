package com.atlassian.jgitflow.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.atlassian.jgitflow.core.exception.JGitFlowGitAPIException;
import com.atlassian.jgitflow.core.report.JGitFlowReportEntry;
import com.atlassian.jgitflow.core.util.GitHelper;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @since version
 */
public class JGitFlowReporter
{
    private static JGitFlowReporter INSTANCE;

    public static final String EOL = System.getProperty("line.separator");
    public static final String P = EOL.concat(EOL);
    public static final String HR = P.concat(Strings.repeat("-", 80)).concat(P);
    public static final int PAD = 4;

    private boolean wroteHeader;
    private boolean clearLog;

    private String header;
    private File logDir;
    private String startTime;
    private int indent;

    private List<JGitFlowReportEntry> entries;
    private List<JGitFlowReportEntry> allEntries;

    private JGitFlowReporter()
    {
        this.wroteHeader = false;
        this.clearLog = false;
        this.entries = newArrayList();
        this.allEntries = newArrayList();

        Date now = new Date();
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzz");
        this.startTime = displayFormat.format(now);

        indent = 0;

    }

    public static JGitFlowReporter get()
    {
        if (null == INSTANCE)
        {
            INSTANCE = new JGitFlowReporter();
        }

        return INSTANCE;
    }

    public void setGitFlowConfiguration(Git git, GitFlowConfiguration config)
    {
        this.logDir = git.getRepository().getDirectory();
        this.header = generateHeader(git, config);
        flush();
    }


    public JGitFlowReporter debugCommandCall(String shortName)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + "## _Command call():_ ", true, false));
        indent += PAD;

        return this;
    }

    public JGitFlowReporter debugText(String shortName, String text)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + "_ " + text + " _", true, false));

        return this;
    }

    public JGitFlowReporter errorText(String shortName, String text)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + "** " + text + " **", false, true));

        return this;
    }

    public JGitFlowReporter commandCall(String shortName)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + "## Command call(): ", false, false));
        indent += PAD;

        return this;
    }

    public JGitFlowReporter endCommand()
    {
        indent -= PAD;

        if (indent < 0)
        {
            indent = 0;
        }

        flush();

        return this;
    }

    public JGitFlowReporter clearLog()
    {
        this.clearLog = true;

        return this;
    }

    public JGitFlowReporter endMethod()
    {
        indent -= PAD;

        if (indent < 0)
        {
            indent = 0;
        }

        entries.add(new JGitFlowReportEntry("", Strings.repeat(" ", indent) + "_method END:_ ", true, false));
        flush();

        return this;
    }

    public JGitFlowReporter debugMethod(String shortName, String text)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + "_method start:_ " + text, true, false));
        indent += PAD;

        return this;
    }

    public JGitFlowReporter infoText(String shortName, String text)
    {
        entries.add(new JGitFlowReportEntry(shortName, Strings.repeat(" ", indent) + text, false, false));

        return this;
    }

    public JGitFlowReporter mergeResult(String shortName, MergeResult mergeResult)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(Strings.repeat(" ", indent))
          .append("### Merge Result")
          .append(EOL)
          .append(mergeResult.toString());

        entries.add(new JGitFlowReportEntry(shortName, sb.toString(), false, false));

        return this;
    }

    public synchronized void flush()
    {
        if (null == logDir || !".git".equals(logDir.getName()))
        {
            return;
        }

        File logFile = new File(logDir, "jgitflow.log");
        Charset utf8 = Charset.forName("UTF-8");
        try
        {
            if (clearLog && null != logDir && logDir.exists())
            {
                if (logFile.exists())
                {
                    logFile.delete();
                }

                Files.touch(logFile);

                clearLog = false;
            }

            if (!clearLog && null == logDir || !logFile.exists())
            {
                logDir.mkdirs();
                Files.touch(logFile);
            }

            if (!wroteHeader && null != header)
            {
                Files.append(header, logFile, utf8);
                wroteHeader = true;
            }

            if (!entries.isEmpty())
            {
                allEntries.addAll(entries);
                List<JGitFlowReportEntry> entriesToWrite = ImmutableList.copyOf(entries);
                this.entries = newArrayList();

                String content = Joiner.on(EOL).join(entriesToWrite) + EOL;
                Files.append(content, logFile, utf8);
            }
        }
        catch (IOException e)
        {
            //ignore
        }

    }

    private String generateHeader(Git git, GitFlowConfiguration config)
    {
        Package gitPkg = Git.class.getPackage();
        String gitVersion = gitPkg.getImplementationVersion();

        Package flowPkg = JGitFlow.class.getPackage();
        String flowVersion = flowPkg.getImplementationVersion();

        StringBuilder sb = new StringBuilder();
        sb.append("# JGitFlow Log - " + startTime)
          .append(P)
          .append("  ## Configuration")
          .append(EOL)
          .append("    JGit Version: ").append(gitVersion)
          .append(EOL)
          .append("    JGitFlow Version: ").append(flowVersion)
          .append(EOL)
          .append("    Master name: ").append(config.getMaster())
          .append(EOL);

        try
        {
            sb.append("    Origin master exists = ").append(GitHelper.remoteBranchExists(git, config.getMaster()))
              .append(EOL);
        }
        catch (JGitFlowGitAPIException e)
        {
            //ignore
        }

        sb.append("    Develop name: ").append(config.getDevelop())
          .append(EOL);

        try
        {
            sb.append("    Origin develop exists = ").append(GitHelper.remoteBranchExists(git, config.getDevelop()))
              .append(EOL);
        }
        catch (JGitFlowGitAPIException e)
        {
            //ignore
        }

        for (String pname : config.getPrefixNames())
        {
            sb.append("    ").append(pname).append(" name: ").append(config.getPrefixValue(pname)).append(EOL);
        }

        sb.append(HR);

        return sb.toString();
    }
}
