package com.atlassian.jgitflow.core.extension;

public interface MasterMergingExtension extends BranchMergingExtension
{
    Iterable<ExtensionCommand> beforeMasterCheckout();

    Iterable<ExtensionCommand> afterMasterCheckout();

    Iterable<ExtensionCommand> beforeMasterMerge();

    Iterable<ExtensionCommand> afterMasterMerge();
}
