package tane.mahuta.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import tane.mahuta.buildtools.version.DefaultSemanticBranchVersion
import tane.mahuta.buildtools.version.SemanticBranchVersion
import tane.mahuta.buildtools.version.SemanticVersion
import tane.mahuta.gradle.plugin.vcs.VcsExtension
import tane.mahuta.gradle.plugin.version.VersionExtension
import tane.mahuta.gradle.plugin.version.VersionParserFactory

import javax.annotation.Nonnull
import javax.annotation.Nullable
/**
 * {@link SemanticVersionPlugin} enhanced by the {@link VcsPlugin} which uses {@link tane.mahuta.buildtools.version.SemanticBranchVersion}s.
 *
 * @author christian.heike@icloud.com
 * Created on 08.06.17.
 */
class SemanticBranchVersionPlugin implements Plugin<Project> {

    @Override
    void apply(@Nonnull final Project target) {
        target.pluginManager.apply(VcsPlugin)
        target.pluginManager.apply(SemanticVersionPlugin)

        final versionExtension = target.version as VersionExtension
        final vcsExtension = target.extensions.findByType(VcsExtension) as VcsExtension

        final toBranchVersion = SemanticBranchVersionPlugin.&toSemanticBranchVersion.ncurry(1, vcsExtension)

        versionExtension.parser = versionExtension.parser.decorate(VersionParserFactory.create(toBranchVersion))
    }

    @Nullable
    private static SemanticBranchVersion toSemanticBranchVersion(@Nullable final SemanticVersion semanticVersion,
                                                                 @Nonnull final VcsExtension vcsExtension) {
        semanticVersion != null ?
                new DefaultSemanticBranchVersion(semanticVersion, SemanticBranchVersionPlugin.&branchNameToQualifier.curry(vcsExtension)) :
                null
    }

    private static String branchNameToQualifier(@Nonnull final VcsExtension vcsExtension) {
        normalizedBranchName(vcsExtension)?.replaceAll("[^a-zA-Z0-9]+", "_")
    }

    private static String normalizedBranchName(@Nonnull final VcsExtension vcsExtension) {
        final name = vcsExtension.branch
        final flowConfig = vcsExtension.flowConfig
        if (name.startsWith(flowConfig.supportBranchPrefix)) {
            return name.replace(flowConfig.supportBranchPrefix, "")
        } else if (name.startsWith(flowConfig.featureBranchPrefix)) {
            return name.replace(flowConfig.featureBranchPrefix, "")
        }
        null
    }

}
