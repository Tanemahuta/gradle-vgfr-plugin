package tane.mahuta.gradle.plugin.release

import groovy.transform.CompileStatic
import org.gradle.StartParameter
import org.gradle.api.internal.BuildDefinition
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.initialization.NestedBuildFactory
import org.gradle.internal.Actions
import org.gradle.internal.build.BuildStateRegistry
import org.gradle.internal.build.DefaultPublicBuildPath
import org.gradle.plugin.management.internal.PluginRequests
import tane.mahuta.buildtools.release.BuildToolAdapter
import tane.mahuta.gradle.plugin.version.VersioningExtension

import javax.annotation.Nonnull

/**
 * The gradle implementation for {@link BuildToolAdapter}.
 * @author christian.heike@icloud.com
 * Created on 29.06.17.
 */
@CompileStatic
class GradleBuildAdapter implements BuildToolAdapter {

    private final ProjectInternal project

    GradleBuildAdapter(@Nonnull final ProjectInternal project) {
        this.project = project
    }

    @Override
    void setVersion(@Nonnull final Object version) {
        project.extensions.findByType(VersioningExtension).setVersion(version)
    }

    @Override
    Object getVersion() {
        project.version
    }

    @Override
    boolean buildRelease() {
        final releaseExtension = project.extensions.findByType(ReleaseExtension)
        final tasks = (releaseExtension.releaseTasks ?: ['check', 'uploadArchives']) as LinkedHashSet
        final buildFactory = project.services.get(NestedBuildFactory) as NestedBuildFactory
        final buildStateRegistry = project.services.get(BuildStateRegistry) as BuildStateRegistry
        final startParameter = (project.services.get(StartParameter.class) as StartParameter).newBuild()

        startParameter.setCurrentDir(project.projectDir)
        startParameter.setProjectDir(project.projectDir)
        startParameter.setTaskNames(tasks)

        final buildDefinition = BuildDefinition.fromStartParameterForBuild(
                startParameter, "release-build",
                project.projectDir,
                PluginRequests.EMPTY,
                Actions.doNothing(),
                new DefaultPublicBuildPath(project.projectPath)
        )
        final nestedBuild = buildStateRegistry.addImplicitIncludedBuild(buildDefinition)
        nestedBuild.execute(tasks, null)
    }
}
