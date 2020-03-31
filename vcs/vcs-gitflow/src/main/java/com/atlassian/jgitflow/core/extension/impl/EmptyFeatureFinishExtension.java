package com.atlassian.jgitflow.core.extension.impl;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.FeatureFinishExtension;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;

public class EmptyFeatureFinishExtension extends EmptyDevelopMergingExtension implements FeatureFinishExtension
{
    private final List<ExtensionCommand> beforeRebase;
    private final List<ExtensionCommand> afterRebase;

    public EmptyFeatureFinishExtension()
    {
        this.beforeRebase = newArrayList();
        this.afterRebase = newArrayList();
    }

    public void addBeforeRebaseCommands(ExtensionCommand... commands)
    {
        beforeRebase.addAll(Arrays.asList(commands));
    }

    public void addAfterRebaseCommands(ExtensionCommand... commands)
    {
        afterRebase.addAll(Arrays.asList(commands));
    }

    @Override
    public Iterable<ExtensionCommand> beforeRebase()
    {
        return Iterables.unmodifiableIterable(beforeRebase);
    }

    @Override
    public Iterable<ExtensionCommand> afterRebase()
    {
        return Iterables.unmodifiableIterable(afterRebase);
    }

}
