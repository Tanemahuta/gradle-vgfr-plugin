package com.atlassian.jgitflow.core.extension;

public interface BranchCreatingExtension extends JGitFlowExtension
{
    Iterable<ExtensionCommand> beforeCreateBranch();

    Iterable<ExtensionCommand> afterCreateBranch();
}
