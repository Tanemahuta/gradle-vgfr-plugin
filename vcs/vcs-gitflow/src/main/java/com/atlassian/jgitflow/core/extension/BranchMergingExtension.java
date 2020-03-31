package com.atlassian.jgitflow.core.extension;

public interface BranchMergingExtension extends JGitFlowExtension
{
    Iterable<ExtensionCommand> afterTopicCheckout();

    Iterable<ExtensionCommand> beforeTag();

    Iterable<ExtensionCommand> afterTag();
}
