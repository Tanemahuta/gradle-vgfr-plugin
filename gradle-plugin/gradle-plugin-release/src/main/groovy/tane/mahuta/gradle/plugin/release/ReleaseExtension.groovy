package tane.mahuta.gradle.plugin.release

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.project.ProjectInternal
import tane.mahuta.buildtools.apilyzer.ApiCompatibilityReportBuilder
import tane.mahuta.buildtools.apilyzer.ApiCompatibilityReportConfiguration
import tane.mahuta.buildtools.apilyzer.clirr.ClirrApiCompatibilityReportBuilder
import tane.mahuta.buildtools.dependency.ArtifactResolver
import tane.mahuta.buildtools.release.ArtifactRelease
import tane.mahuta.buildtools.release.DefaultReleaseInfrastructure
import tane.mahuta.buildtools.release.FunctionalVersionHandler
import tane.mahuta.buildtools.release.ReleaseInfrastructure
import tane.mahuta.gradle.plugin.VersionPlugin
import tane.mahuta.gradle.plugin.release.resolver.GradleArtifactResolver
import tane.mahuta.gradle.plugin.vcs.VcsExtension
import tane.mahuta.gradle.plugin.version.VersioningExtension

import javax.annotation.Nonnull
import javax.annotation.Nullable

/**
 * Extension for the {@link tane.mahuta.gradle.plugin.ReleasePlugin}.
 *
 * @author christian.heike@icloud.com
 * Created on 08.06.17.
 */
@CompileStatic
class ReleaseExtension {

    private final ProjectInternal project
    private final ArtifactResolver artifactResolver

    private ReleaseInfrastructure releaseInfrastructure
    private Set<ArtifactRelease> artifactReleases

    /**
     * The {@link ApiCompatibilityReportBuilder.Factory} to use when creating the report builder
     */
    ApiCompatibilityReportBuilder.Factory apiReportBuilderFactory

    /**
     * The tasks to run when releasing
     */
    List<String> releaseTasks

    private Closure apiReportConfiguration

    ReleaseExtension(@Nonnull final ProjectInternal project) {
        this.project = project
        this.artifactResolver = new GradleArtifactResolver(project)
    }

    protected VersioningExtension getVersionExtension() {
        findExtension(VersioningExtension)
    }

    protected VcsExtension getVcs() {
        findExtension(VcsExtension)
    }

    /**
     * Set the closure
     * @param reportConfiguration
     */
    ReleaseExtension setApiReportConfiguration(
            @Nullable @DelegatesTo(ApiCompatibilityReportConfiguration) final Closure apiReportConfiguration) {
        this.apiReportConfiguration = apiReportConfiguration
        this
    }

    /**
     * @return the infrastructure for the project
     */
    @Nonnull
    ReleaseInfrastructure getInfrastructure() {
        releaseInfrastructure = (releaseInfrastructure ?: factorReleaseInfrastructure()) as ReleaseInfrastructure
    }

    /**
     * @return the artifact releases for the project
     */
    @Nonnull
    Set<ArtifactRelease> getArtifactReleases() {
        artifactReleases = (artifactReleases ?: factorArtifactReleases()) as Set<ArtifactRelease>
    }

    @Nonnull
    private ReleaseInfrastructure factorReleaseInfrastructure() {
        DefaultReleaseInfrastructure.builder().artifactResolver(artifactResolver)
                .versionHandler(factorVersionHandler())
                .vcs(vcs)
                .buildToolAdapter(new GradleBuildAdapter(project))
                .apiCompatibilityReportBuilderFactory(getOrDefaultApiCompatibilityReportBuilderFactory())
                .versionStorage(versionExtension.storage)
                .build()
    }

    private ApiCompatibilityReportBuilder.Factory getOrDefaultApiCompatibilityReportBuilderFactory() {
        return new ApiCompatibilityReportBuilder.Factory() {
            @Override
            ApiCompatibilityReportBuilder builder() {
                final ApiCompatibilityReportBuilder builder =
                        (apiReportBuilderFactory?.builder() ?: new ClirrApiCompatibilityReportBuilder()) as ApiCompatibilityReportBuilder
                apiReportConfiguration?.delegate = builder
                apiReportConfiguration?.resolveStrategy == Closure.DELEGATE_FIRST
                apiReportConfiguration?.call()
                return builder
            }
        }
    }

    @Nonnull
    private FunctionalVersionHandler factorVersionHandler() {
        Objects.requireNonNull(versionExtension.parser, "Could not find version parser, please set project.${VersionPlugin.EXTENSION}.parser")
        Objects.requireNonNull(versionExtension.storage, "Could not find version storage, please set project.${VersionPlugin.EXTENSION}.storage")
        Objects.requireNonNull(versionExtension.releaseTransformer, "Could not find release transformer, please set project.${VersionPlugin.EXTENSION}.releaseTransformer")
        Objects.requireNonNull(versionExtension.releaseTransformerForReport, "Could not find release transformer, please set project.${VersionPlugin.EXTENSION}.releaseTransformerForReport")
        Objects.requireNonNull(versionExtension.comparator, "Could not find version comparator, please set project.${VersionPlugin.EXTENSION}.comparator")
        FunctionalVersionHandler.builder()
                .parser(versionExtension.parser)
                .storage(versionExtension.storage)
                .toReleaseVersionHandler(versionExtension.releaseTransformer)
                .toReleaseVersionWithReportHandler(versionExtension.releaseTransformerForReport)
                .toNextDevelopmentVersionHandler(versionExtension.nextDevelopmentTransformer)
                .comparator(versionExtension.comparator).build()
    }

    @Nonnull
    private Set<ArtifactRelease> factorArtifactReleases() {
        (project.configurations
                .collect { Configuration config ->  config.artifacts }
                .flatten() as Collection<PublishArtifact>)
                .collect {
            GradleDomainObjectAdapter.instance.createArtifactRelease(project, it)
        } as Set<ArtifactRelease>
    }

    @Nullable
    private <E> E findExtension(@Nonnull final Class<E> extensionClass) {
        def curProject = project
        while (curProject) {
            final extension = curProject.extensions.findByType(extensionClass)
            if (extension) {
                return extension
            }
            curProject = curProject.parent
        }
    }

}
