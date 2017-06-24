package tane.mahuta.buildtools.release.check;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tane.mahuta.buildtools.dependency.GAVCDescriptor;
import tane.mahuta.buildtools.release.ArtifactRelease;
import tane.mahuta.buildtools.release.ReleaseInfrastructure;
import tane.mahuta.buildtools.release.ReleaseStep;
import tane.mahuta.buildtools.release.reporting.Severity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks if the project references any snapshot dependencies.
 *
 * @author christian.heike@icloud.com
 *         Created on 23.06.17.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReferencesSnapshotDependenciesCheck implements ReleaseStep {

    private static class InstanceHolder {
        private static final ReferencesSnapshotDependenciesCheck INSTANCE = new ReferencesSnapshotDependenciesCheck();
    }

    public static ReferencesSnapshotDependenciesCheck getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void apply(@Nonnull final ArtifactRelease release, @Nonnull final ReleaseInfrastructure releaseInfrastructure) {
        release.getDependencyContainers().forEach(container -> {

            final List<String> snapshotDependencies = container.getDependencies().stream()
                    .filter(GAVCDescriptor::isSnapshot)
                    .map(GAVCDescriptor::toStringDescriptor)
                    .collect(Collectors.toList());

            if (!snapshotDependencies.isEmpty()) {
                release.describeProblem(b -> b.severity(Severity.PROBLEM)
                        .messageFormat("Dependency container {} contains snapshot dependencies: {}")
                        .formatArgs(container.getName(), snapshotDependencies));
            }
        });
    }

}
