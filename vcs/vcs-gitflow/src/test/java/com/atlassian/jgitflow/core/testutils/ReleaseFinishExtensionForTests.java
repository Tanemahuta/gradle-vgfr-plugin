package com.atlassian.jgitflow.core.testutils;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.ReleaseFinishExtension;

import com.google.common.collect.Lists;

public class ReleaseFinishExtensionForTests extends BaseExtensionForTests<ReleaseFinishExtensionForTests> implements ReleaseFinishExtension
{
    @Override
    public Iterable<ExtensionCommand> beforeMasterCheckout()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_MASTER_CHECKOUT));
    }

    @Override
    public Iterable<ExtensionCommand> afterMasterCheckout()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_MASTER_CHECKOUT));
    }

    @Override
    public Iterable<ExtensionCommand> beforeMasterMerge()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_MASTER_MERGE));
    }

    @Override
    public Iterable<ExtensionCommand> afterMasterMerge()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_MASTER_MERGE));
    }

    @Override
    public Iterable<ExtensionCommand> beforeDevelopCheckout()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_DEVELOP_CHECKOUT));
    }

    @Override
    public Iterable<ExtensionCommand> afterDevelopCheckout()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_DEVELOP_CHECKOUT));
    }

    @Override
    public Iterable<ExtensionCommand> beforeDevelopMerge()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_DEVELOP_MERGE));
    }

    @Override
    public Iterable<ExtensionCommand> afterDevelopMerge()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_DEVELOP_MERGE));
    }

    @Override
    public Iterable<ExtensionCommand> afterPush()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_PUSH));
    }

    @Override
    public Iterable<ExtensionCommand> afterTopicCheckout()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_TOPIC_CHECKOUT));
    }

    @Override
    public Iterable<ExtensionCommand> beforeTag()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_TAG));
    }

    @Override
    public Iterable<ExtensionCommand> afterTag()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_TAG));
    }
}
