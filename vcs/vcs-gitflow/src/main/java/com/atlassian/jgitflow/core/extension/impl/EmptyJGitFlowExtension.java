package com.atlassian.jgitflow.core.extension.impl;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.JGitFlowExtension;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;

public abstract class EmptyJGitFlowExtension implements JGitFlowExtension
{
    private final List<ExtensionCommand> before;
    private final List<ExtensionCommand> after;
    private final List<ExtensionCommand> beforeFetch;
    private final List<ExtensionCommand> afterFetch;
    private final List<ExtensionCommand> afterPush;

    protected EmptyJGitFlowExtension()
    {
        this.before = newArrayList();
        this.after = newArrayList();
        this.beforeFetch = newArrayList();
        this.afterFetch = newArrayList();
        this.afterPush = newArrayList();
    }

    public void addBeforeCommands(ExtensionCommand... commands)
    {
        before.addAll(Arrays.asList(commands));
    }

    public void addAfterCommands(ExtensionCommand... commands)
    {
        after.addAll(Arrays.asList(commands));
    }

    public void addBeforeFetchCommands(ExtensionCommand... commands)
    {
        beforeFetch.addAll(Arrays.asList(commands));
    }

    public void addAfterFetchCommands(ExtensionCommand... commands)
    {
        afterFetch.addAll(Arrays.asList(commands));
    }

    public void addAfterPushCommands(ExtensionCommand... commands)
    {
        afterPush.addAll(Arrays.asList(commands));
    }

    @Override
    public Iterable<ExtensionCommand> before()
    {
        return Iterables.unmodifiableIterable(before);
    }

    @Override
    public Iterable<ExtensionCommand> after()
    {
        return Iterables.unmodifiableIterable(after);
    }

    @Override
    public Iterable<ExtensionCommand> beforeFetch()
    {
        return Iterables.unmodifiableIterable(beforeFetch);
    }

    @Override
    public Iterable<ExtensionCommand> afterFetch()
    {
        return Iterables.unmodifiableIterable(afterFetch);
    }

    @Override
    public Iterable<ExtensionCommand> afterPush()
    {
        return Iterables.unmodifiableIterable(afterPush);
    }
}
