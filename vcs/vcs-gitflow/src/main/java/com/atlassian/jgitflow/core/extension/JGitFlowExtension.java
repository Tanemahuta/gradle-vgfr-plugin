package com.atlassian.jgitflow.core.extension;

public interface JGitFlowExtension
{
    Iterable<ExtensionCommand> before();

    Iterable<ExtensionCommand> after();

    Iterable<ExtensionCommand> beforeFetch();

    Iterable<ExtensionCommand> afterFetch();

    Iterable<ExtensionCommand> afterPush();
}
