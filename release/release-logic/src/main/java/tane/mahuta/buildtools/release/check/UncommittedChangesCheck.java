package tane.mahuta.buildtools.release.check;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tane.mahuta.buildtools.release.AbstractGuardedReleaseStep;
import tane.mahuta.buildtools.release.ArtifactRelease;
import tane.mahuta.buildtools.release.ReleaseInfrastructure;
import tane.mahuta.buildtools.release.reporting.Severity;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Checks if the there are uncommitted changes and reports it to the project.
 *
 * @author christian.heike@icloud.com
 *         Created on 23.06.17.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UncommittedChangesCheck extends AbstractGuardedReleaseStep {

    private static class InstanceHolder {
        private static final UncommittedChangesCheck INSTANCE = new UncommittedChangesCheck();
    }

    public static UncommittedChangesCheck getInstance() {
        return UncommittedChangesCheck.InstanceHolder.INSTANCE;
    }

    @Override
    protected void doApply(@Nonnull final ArtifactRelease release,
                           @Nonnull final ReleaseInfrastructure releaseInfrastructure,
                           @Nonnull final Object releaseVersion) {
        final Collection<String> uncommitted = releaseInfrastructure.getVcs().getUncommittedFilePaths();
        if (!uncommitted.isEmpty()) {
            release.describeProblem(b -> b.severity(Severity.PROBLEM)
                    .messageFormat("Found uncommitted changes: {}")
                    .formatArgs(uncommitted));
        }
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Checks if there are uncommitted VCS changes";
    }
}
