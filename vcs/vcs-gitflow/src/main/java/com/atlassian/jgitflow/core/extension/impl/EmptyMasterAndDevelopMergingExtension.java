package com.atlassian.jgitflow.core.extension.impl;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.MasterMergingExtension;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;

public abstract class EmptyMasterAndDevelopMergingExtension extends EmptyDevelopMergingExtension implements MasterMergingExtension
{
    private final List<ExtensionCommand> beforeMasterCheckout;
    private final List<ExtensionCommand> afterMasterCheckout;
    private final List<ExtensionCommand> beforeMasterMerge;
    private final List<ExtensionCommand> afterMasterMerge;


    protected EmptyMasterAndDevelopMergingExtension()
    {
        this.beforeMasterCheckout = newArrayList();
        this.afterMasterCheckout = newArrayList();
        this.beforeMasterMerge = newArrayList();
        this.afterMasterMerge = newArrayList();
    }

    public void addBeforeMasterCheckoutCommands(ExtensionCommand... commands)
    {
        beforeMasterCheckout.addAll(Arrays.asList(commands));
    }

    public void addAfterMasterCheckoutCommands(ExtensionCommand... commands)
    {
        afterMasterCheckout.addAll(Arrays.asList(commands));
    }

    public void addBeforeMasterMergeCommands(ExtensionCommand... commands)
    {
        beforeMasterMerge.addAll(Arrays.asList(commands));
    }

    public void addAfterMasterMergeCommands(ExtensionCommand... commands)
    {
        afterMasterMerge.addAll(Arrays.asList(commands));
    }

    @Override
    public Iterable<ExtensionCommand> beforeMasterCheckout()
    {
        return Iterables.unmodifiableIterable(beforeMasterCheckout);
    }

    @Override
    public Iterable<ExtensionCommand> afterMasterCheckout()
    {
        return Iterables.unmodifiableIterable(afterMasterCheckout);
    }

    @Override
    public Iterable<ExtensionCommand> beforeMasterMerge()
    {
        return Iterables.unmodifiableIterable(beforeMasterMerge);
    }

    @Override
    public Iterable<ExtensionCommand> afterMasterMerge()
    {
        return Iterables.unmodifiableIterable(afterMasterMerge);
    }

}
