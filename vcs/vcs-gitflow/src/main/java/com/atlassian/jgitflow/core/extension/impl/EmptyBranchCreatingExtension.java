package com.atlassian.jgitflow.core.extension.impl;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jgitflow.core.extension.BranchCreatingExtension;
import com.atlassian.jgitflow.core.extension.ExtensionCommand;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;

public abstract class EmptyBranchCreatingExtension extends EmptyJGitFlowExtension implements BranchCreatingExtension
{
    private final List<ExtensionCommand> beforeCreateBranch;
    private final List<ExtensionCommand> afterCreateBranch;

    protected EmptyBranchCreatingExtension()
    {
        this.beforeCreateBranch = newArrayList();
        this.afterCreateBranch = newArrayList();
    }

    public void addBeforeCreateBranchCommands(ExtensionCommand... commands)
    {
        beforeCreateBranch.addAll(Arrays.asList(commands));
    }

    public void addAfterCreateBranchCommands(ExtensionCommand... commands)
    {
        afterCreateBranch.addAll(Arrays.asList(commands));
    }

    @Override
    public Iterable<ExtensionCommand> beforeCreateBranch()
    {
        return Iterables.unmodifiableIterable(beforeCreateBranch);
    }

    @Override
    public Iterable<ExtensionCommand> afterCreateBranch()
    {
        return Iterables.unmodifiableIterable(afterCreateBranch);
    }
}
