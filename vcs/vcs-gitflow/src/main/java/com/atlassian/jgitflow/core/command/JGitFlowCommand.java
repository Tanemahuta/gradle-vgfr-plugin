package com.atlassian.jgitflow.core.command;

public interface JGitFlowCommand
{

    Object setAllowUntracked(boolean allow);

    boolean isAllowUntracked();

    String getScmMessagePrefix();

    Object setScmMessagePrefix(String scmMessagePrefix);

    String getScmMessageSuffix();

    Object setScmMessageSuffix(String scmMessageSuffix);

    Object setFetch(boolean fetch);

    boolean isFetch();

    Object setPush(boolean push);

    boolean isPush();

    String getBranchName();
}
