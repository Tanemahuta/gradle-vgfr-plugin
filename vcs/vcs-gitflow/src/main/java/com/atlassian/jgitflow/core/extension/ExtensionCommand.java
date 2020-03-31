package com.atlassian.jgitflow.core.extension;

import com.atlassian.jgitflow.core.GitFlowConfiguration;
import com.atlassian.jgitflow.core.command.JGitFlowCommand;
import com.atlassian.jgitflow.core.exception.JGitFlowExtensionException;

import org.eclipse.jgit.api.Git;

public interface ExtensionCommand
{
    void execute(GitFlowConfiguration configuration, Git git, JGitFlowCommand gitFlowCommand) throws JGitFlowExtensionException;

    ExtensionFailStrategy failStrategy();
}
