package tane.mahuta.gradle.plugin

import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import tane.mahuta.buildtools.release.ReleaseStep
import tane.mahuta.buildtools.release.check.*
import tane.mahuta.gradle.plugin.release.ReleaseCheckReportTask
import tane.mahuta.gradle.plugin.release.ReleaseExtension
import tane.mahuta.gradle.plugin.release.ReleaseExtensionTask
import tane.mahuta.gradle.plugin.release.ReleaseTask

import javax.annotation.Nonnull

/**
 * A plugin which uses {@link VcsPlugin} and {@link VersionPlugin} for releasing.
 *
 * @author christian.heike@icloud.com
 * Created on 08.06.17.
 */
class ReleasePlugin implements Plugin<Project> {

    static final String EXTENSION = "release"

    static final String TASK_RELEASE_CHECK = "releaseCheck"
    static final String TASK_RELEASE = "release"

    @Override
    void apply(final Project target) {
        final Set<String> appliedTo = findAlreadyAppliedTo(target)
        if (!appliedTo.isEmpty()) {
            throw new InvalidUserCodeException("Release plugin already applied to sub/parent projects: ${appliedTo.join(', ')}")
        }

        target.getPluginManager().apply(VersionPlugin)
        target.getPluginManager().apply(VcsPlugin)

        target.allprojects.each {
            it.extensions.create(EXTENSION, ReleaseExtension, it)
            target.logger.info("Created release extension for {}", it)
        }
        final checkTasks = [createCheckTask(target, UncommittedChangesCheck.instance),
                            createCheckTask(target, ReleasableBranchCheck.instance),
                            createCheckTask(target, ReferencesSnapshotDependenciesCheck.instance),
                            createCheckTask(target, NotAlreadyReleasedCheck.instance),
                            createCheckTask(target, ReleaseVersionMatchesApiCompatibilityCheck.instance)]
        final releaseCheckTask = target.task(TASK_RELEASE_CHECK, type: ReleaseCheckReportTask).dependsOn(
                checkTasks
        )

        target.allprojects.each {
            it.afterEvaluate { project ->
                ['check', 'assemble'].each {
                    final Task dep = project.tasks.findByName(it)
                    if (dep) {
                        checkTasks.each{ check -> check.dependsOn(dep) }
                    }
                }
            }
        }

        target.task(TASK_RELEASE, type: ReleaseTask).dependsOn(releaseCheckTask)
    }

    private Set<String> findAlreadyAppliedTo(Project target) {
        final appliedTo = target.subprojects.findAll { it.plugins.hasPlugin(getClass()) }.collect { it.path } as List
        def curProject = target.parent
        while (curProject != null) {
            if (curProject.plugins.hasPlugin(getClass())) {
                appliedTo << curProject.path
            }
            curProject = curProject.parent
        }
        appliedTo as Set
    }

    static Task createCheckTask(@Nonnull final Project project,
                                @Nonnull final ReleaseStep releaseStep) {
        final String name = releaseStep.getClass().simpleName.replaceAll(/Check$/, "")
        createStepTask(project, TASK_RELEASE_CHECK + name.capitalize(), releaseStep)
    }

    static Task createStepTask(@Nonnull final Project project,
                               @Nonnull final String name,
                               @Nonnull final ReleaseStep releaseStep) {
        project.task(name, type: ReleaseExtensionTask, description: releaseStep.description, {
            doFirst {
                (delegate as ReleaseExtensionTask).invokeStep(releaseStep)
            }
        })
    }

}
