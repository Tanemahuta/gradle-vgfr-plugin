package com.atlassian.jgitflow.core.extension;

public interface DevelopMergingExtension extends BranchMergingExtension
{
    Iterable<ExtensionCommand> beforeDevelopCheckout();

    Iterable<ExtensionCommand> afterDevelopCheckout();

    Iterable<ExtensionCommand> beforeDevelopMerge();

    Iterable<ExtensionCommand> afterDevelopMerge();
}
