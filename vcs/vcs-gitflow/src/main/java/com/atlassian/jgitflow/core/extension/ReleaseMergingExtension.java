package com.atlassian.jgitflow.core.extension;

public interface ReleaseMergingExtension extends BranchMergingExtension
{
    Iterable<ExtensionCommand> beforeReleaseCheckout();

    Iterable<ExtensionCommand> afterReleaseCheckout();

    Iterable<ExtensionCommand> beforeReleaseMerge();

    Iterable<ExtensionCommand> afterReleaseMerge();
}
