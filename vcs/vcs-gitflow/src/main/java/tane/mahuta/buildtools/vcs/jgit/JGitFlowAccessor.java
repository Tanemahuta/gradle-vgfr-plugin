package tane.mahuta.buildtools.vcs.jgit;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.JGitFlowWithConfig;
import com.atlassian.jgitflow.core.ReleaseMergeResult;
import com.atlassian.jgitflow.core.command.AbstractBranchMergingCommand;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import tane.mahuta.buildtools.vcs.VcsAccessor;
import tane.mahuta.buildtools.vcs.VcsFlowConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link VcsAccessor} using {@link com.atlassian.jgitflow.core.JGitFlow}.
 *
 * @author christian.heike@icloud.com
 * Created on 06.06.17.
 */
@Slf4j
public class JGitFlowAccessor implements VcsAccessor {

    private final JGitFlowWithConfig jGitFlowWithConfig;

    @Getter(onMethod = @__({@Override, @Nonnull}))
    private final VcsFlowConfig flowConfig;

    public JGitFlowAccessor(@Nonnull final JGitFlowWithConfig jGitFlowWithConfig) {
        this.jGitFlowWithConfig = jGitFlowWithConfig;
        this.flowConfig = new JGitFlowConfig(jGitFlowWithConfig.getConfiguration());
    }

    @Override
    @Nullable
    public String getBranch() {
        return Stream.of("BRANCH_NAME", "GIT_BRANCH", "CI_COMMIT_REF_NAME")
                .map(System::getenv)
                .filter(Objects::nonNull).findFirst().orElseGet(this::getRepositoryBranch);
    }

    @SneakyThrows
    private String getRepositoryBranch() {
        return getJGitFlow().git().getRepository().getBranch();
    }

    @Override
    @Nullable
    @SneakyThrows
    public String getRevisionId() {
        return Optional.ofNullable(getJGitFlow().git().getRepository().resolve(Constants.HEAD)).map(AnyObjectId::name).orElse(null);
    }

    @Nonnull
    @Override
    @SneakyThrows
    public Collection<String> getUncommittedFilePaths() {
        final Set<String> result = new HashSet<>();
        final Status status = getJGitFlow().git().status().call();
        result.addAll(status.getUncommittedChanges());
        result.addAll(status.getUntracked());
        return result;
    }

    @SneakyThrows
    @Override
    public void startReleaseBranch(@Nonnull final String version) {
        if (isOnBranch(flowConfig.getReleaseBranchPrefix()) || isOnBranch(flowConfig.getHotfixBranchPrefix())) {
            log.info("Not starting a release branch, since we are on branch: {}", getBranch());
            return;
        }
        log.debug("Starting release for version: {}", version);
        final Ref startResult = getJGitFlow().releaseStart(version).call();
        log.info("Started release for version {}: {}", version, startResult.getName());
        getJGitFlow().releasePublish(version).call();
        log.info("Published release for version: {}", version);
    }


    @Override
    @SneakyThrows
    public boolean finishReleaseBranch(@Nonnull final String version) {
        if (isOnBranch(flowConfig.getHotfixBranchPrefix())) {
            log.debug("Finishing hotfix branch for version: {}", version);
            return handleAndReturn(getJGitFlow().hotfixFinish(version).setMessage("Release ${version}").setPush(true));
        } else if (isOnBranch(flowConfig.getReleaseBranchPrefix())) {
            log.debug("Finishing release branch for version: {}", version);
            return handleAndReturn(getJGitFlow().releaseFinish(version).setMessage("Release ${version}").setPush(true));
        } else {
            log.error("Could not determine correct path to finish branch: {}", getBranch());
            return false;
        }
    }

    @Override
    @SneakyThrows
    public void commitFiles(@Nonnull final String message) {
        final Git git = getJGitFlow().git();
        git.add().addFilepattern(".").call();
        git.commit().setAll(true).setMessage(message).call();
    }

    @Override
    @SneakyThrows
    public void push() {
        gitPushWithCredentials().call();
    }

    @Override
    @SneakyThrows
    public void checkout(@Nonnull final String branch) {
        getJGitFlow().git().checkout().setName(branch).call();
    }

    @Override
    @SneakyThrows
    public void pushTags() {
        gitPushWithCredentials().setPushTags().call();
    }

    private PushCommand gitPushWithCredentials() {
        final PushCommand result = getJGitFlow().git().push();
        getCredentialsProvider().ifPresent(result::setCredentialsProvider);
        return result;
    }

    private Optional<CredentialsProvider> getCredentialsProvider() {
        final String username = System.getenv("GIT_USERNAME");
        final String password = System.getenv("GIT_PASSWORD");
        if (username != null && password != null) {
            return Optional.of(new UsernamePasswordCredentialsProvider(username, password));
        }
        return Optional.empty();
    }


    private JGitFlow getJGitFlow() {
        return jGitFlowWithConfig;
    }

    private static <T extends AbstractBranchMergingCommand<T, ReleaseMergeResult>> boolean handleAndReturn(final T command) throws Exception {
        final ReleaseMergeResult result = command.call();
        if (result.developHasProblems()) {
            log.error("Problems while merging to development branch: {}", createDescription(result.getDevelopResult().getFailingPaths()));
        }
        if (result.masterHasProblems()) {
            log.error("Problems while merging to master branch: {}", createDescription(result.getMasterResult().getFailingPaths()));
        }
        return result.wasSuccessful();
    }

    private static StringBuilder createDescription(final Map<String, ResolveMerger.MergeFailureReason> problems) {
        final StringBuilder sb = new StringBuilder();
        problems.forEach((path, reason) -> sb.append(System.getProperty("line.separator")).append("Path ").append(path).append(": ").append(reason));
        return sb;
    }

}
