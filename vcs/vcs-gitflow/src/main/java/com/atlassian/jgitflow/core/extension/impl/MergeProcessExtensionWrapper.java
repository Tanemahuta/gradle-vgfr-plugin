package com.atlassian.jgitflow.core.extension.impl;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;

public class MergeProcessExtensionWrapper
{
    private final Iterable<ExtensionCommand> beforeCheckout;
    private final Iterable<ExtensionCommand> afterCheckout;
    private final Iterable<ExtensionCommand> beforeMerge;
    private final Iterable<ExtensionCommand> afterMerge;

    public MergeProcessExtensionWrapper(Iterable<ExtensionCommand> beforeCheckout, Iterable<ExtensionCommand> afterCheckout, Iterable<ExtensionCommand> beforeMerge, Iterable<ExtensionCommand> afterMerge)
    {
        this.beforeCheckout = beforeCheckout;
        this.afterCheckout = afterCheckout;
        this.beforeMerge = beforeMerge;
        this.afterMerge = afterMerge;
    }

    public Iterable<ExtensionCommand> beforeCheckout() {return this.beforeCheckout;}

    public Iterable<ExtensionCommand> afterCheckout() {return this.afterCheckout;}

    public Iterable<ExtensionCommand> beforeMerge() {return this.beforeMerge;}

    public Iterable<ExtensionCommand> afterMerge() {return this.afterMerge;}
}
