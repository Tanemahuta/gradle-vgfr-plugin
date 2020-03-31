package com.atlassian.jgitflow.core.testutils;

import com.atlassian.jgitflow.core.extension.ExtensionCommand;
import com.atlassian.jgitflow.core.extension.FeatureStartExtension;

import com.google.common.collect.Lists;

public class FeatureStartExtensionForTests extends BaseExtensionForTests<FeatureStartExtensionForTests> implements FeatureStartExtension
{
    @Override
    public Iterable<ExtensionCommand> beforeCreateBranch()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(BEFORE_CREATE_BRANCH));
    }

    @Override
    public Iterable<ExtensionCommand> afterCreateBranch()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_CREATE_BRANCH));
    }

    @Override
    public Iterable<ExtensionCommand> afterPush()
    {
        return Lists.<ExtensionCommand>newArrayList(createExtension(AFTER_PUSH));
    }

}
