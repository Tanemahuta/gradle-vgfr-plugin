package tane.mahuta.gradle.plugin.version

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import org.gradle.api.Project
import tane.mahuta.buildtools.apilyzer.ApiCompatibilityReport
import tane.mahuta.buildtools.version.SemanticVersion
import tane.mahuta.buildtools.version.VersionParser
import tane.mahuta.buildtools.version.VersionStorage

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author christian.heike@icloud.com
 * Created on 04.06.17.
 */
@CompileStatic
@EqualsAndHashCode
class VersioningExtension {

    private final ProjectVersionAccessor versionAccessor
    private final File sourceDirectory

    /**
     * the storage to be used to store the version
     */
    VersionStorage storage
    /**
     * the parser to be used to parse the version
     */
    VersionParser<?> parser

    /**
     * The release version transformer
     */
    Function<?, ?> releaseTransformer
    /**
     * The release version transformer for an {@link ApiCompatibilityReport}
     */
    BiFunction<?, ApiCompatibilityReport, ?> releaseTransformerForReport
    /**
     * The comparator for parsed versions
     */
    Comparator<?> comparator
    /**
     * The next development iteration version transformer
     */
    Function<?, ?> nextDevelopmentTransformer

    /**
     * Creates a new extension for the project.
     * @param project the project
     */
    VersioningExtension(@Nonnull final Project project) {
        this.versionAccessor = new ProjectVersionAccessor(project)
        this.sourceDirectory = project.projectDir
    }

    /**
     * Set the parser. In case the parser is not {@code null}, the version is being parsed.
     * @param parser the parser to set
     */
    void setParser(@Nullable final VersionParser<?> parser) {
        this.parser = parser
        reparse()
    }

    /**
     * Set the parser using a closure.
     * @param parser the parser to be set
     */
    void setParserClosure(@Nullable
                          @ClosureParams(value = FromString, options = ["java.lang.Object", "java.io.File"])
                          final Closure<?> parser) {
        setParser(parser != null ? VersionParserFactory.create(parser) : null)
    }

    /**
     * Set a closure as the comparator.
     * @param comparator the closure comparing two versions
     */
    void setComparatorClosure(@Nonnull
                              @ClosureParams(value = FromString, options = ["java.lang.Object", "java.lang.Object"])
                              final Closure<Integer> comparator) {
        this.comparator = new Comparator<Object>() {
            @Override
            int compare(final Object o1, final Object o2) {
                comparator.call(o1, o2)
            }
        }
    }

    /**
     * Set a closure for transforming the version to a release version.
     * @param transformer the transformation closure
     */
    void setReleaseTransformerClosure(@Nonnull
                                      @ClosureParams(value = FromString, options = ["java.lang.Object"])
                                      final Closure<?> transformer) {
        this.releaseTransformer = new Function<Object, Object>() {
            @Override
            Object apply(@Nonnull final Object o) {
                return transformer.call(o)
            }
        }
    }

    /**
     * Set a closure for transforming the version to a release version using an {@link ApiCompatibilityReport}
     * @param transformer the transformation closure
     */
    void setReleaseTransformerForReportClosure(@Nonnull
                                               @ClosureParams(value = FromString, options = ["java.lang.Object", "tane.mahuta.buildtools.apilyzer.ApiCompatibilityReport"])
                                               final Closure<?> transformer) {
        this.releaseTransformerForReport = new BiFunction<Object, ApiCompatibilityReport, Object>() {
            @Override
            Object apply(@Nonnull final Object o, @Nonnull final ApiCompatibilityReport compatibilityReport) {
                return transformer.call(o, compatibilityReport)
            }
        }
    }

    /**
     * Set a closure for transforming the version to a release version.
     * @param transformer the transformation closure
     */
    void setNextDevelopmentTransformerClosure(@Nonnull
                                              @ClosureParams(value = FromString, options = ["java.lang.Object"])
                                              final Closure<?> transformer) {
        this.nextDevelopmentTransformer = new Function<Object, Object>() {
            @Override
            Object apply(@Nonnull final Object o) {
                return transformer.call(o)
            }
        }
    }

    /**
     * Set the version and parses it, if a
     * @param version
     */
    void setVersion(final Object version) {
        this.versionAccessor.set(version)
        reparse()
    }

    private void reparse() {
        final sourceObj = this.versionAccessor.get()
        final source = sourceObj instanceof SemanticVersion ? (sourceObj as SemanticVersion).parseableString : sourceObj as String
        if (this.parser != null && source != null) {
            this.versionAccessor.set(parser.parse(source, this.sourceDirectory))
        }
    }
}
