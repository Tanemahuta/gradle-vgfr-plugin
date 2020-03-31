package com.atlassian.jgitflow.core.extension;

public interface FeatureFinishExtension extends DevelopMergingExtension
{
    Iterable<ExtensionCommand> beforeRebase();

    Iterable<ExtensionCommand> afterRebase();


}
