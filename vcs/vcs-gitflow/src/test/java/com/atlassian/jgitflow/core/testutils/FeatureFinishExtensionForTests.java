package com.atlassian.jgitflow.core.testutils;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.FeatureFinishExtension;

import com.google.common.collect.Lists;

public class FeatureFinishExtensionForTests extends BaseExtensionForTests<FeatureFinishExtensionForTests> implements FeatureFinishExtension
{
    @Override
    public Iterable<ExtensionCommand> beforeRebase()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_REBASE));
    }

    @Override
    public Iterable<ExtensionCommand> afterRebase()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_REBASE));
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
