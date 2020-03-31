package com.atlassian.jgitflow.core.extension.impl;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.ReleaseMergingExtension;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;

public class EmptyMasterAndDevelopAndReleaseMergingExtension extends EmptyMasterAndDevelopMergingExtension implements ReleaseMergingExtension
{

    private final List<ExtensionCommand> beforeReleaseCheckout;
    private final List<ExtensionCommand> afterReleaseCheckout;
    private final List<ExtensionCommand> beforeReleaseMerge;
    private final List<ExtensionCommand> afterReleaseMerge;


    protected EmptyMasterAndDevelopAndReleaseMergingExtension()
    {
        this.beforeReleaseCheckout = newArrayList();
        this.afterReleaseCheckout = newArrayList();
        this.beforeReleaseMerge = newArrayList();
        this.afterReleaseMerge = newArrayList();
    }

    public void addBeforeReleaseCheckoutCommands(ExtensionCommand... commands)
    {
        beforeReleaseCheckout.addAll(Arrays.asList(commands));
    }

    public void addAfterReleaseCheckoutCommands(ExtensionCommand... commands)
    {
        afterReleaseCheckout.addAll(Arrays.asList(commands));
    }

    public void addBeforeReleaseMergeCommands(ExtensionCommand... commands)
    {
        beforeReleaseMerge.addAll(Arrays.asList(commands));
    }

    public void addAfterReleaseMergeCommands(ExtensionCommand... commands)
    {
        afterReleaseMerge.addAll(Arrays.asList(commands));
    }

    @Override
    public Iterable<ExtensionCommand> beforeReleaseCheckout()
    {
        return Iterables.unmodifiableIterable(beforeReleaseCheckout);
    }

    @Override
    public Iterable<ExtensionCommand> afterReleaseCheckout()
    {
        return Iterables.unmodifiableIterable(afterReleaseCheckout);
    }

    @Override
    public Iterable<ExtensionCommand> beforeReleaseMerge()
    {
        return Iterables.unmodifiableIterable(beforeReleaseMerge);
    }

    @Override
    public Iterable<ExtensionCommand> afterReleaseMerge()
    {
        return Iterables.unmodifiableIterable(afterReleaseMerge);
    }
}
