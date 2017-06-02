package tane.mahuta.gradle.plugins.version

import groovy.transform.CompileStatic

import javax.annotation.Nonnull

/**
 * Transformer for {@link SemanticVersion}s.
 * @author christian.heike@icloud.com
 * Created on 23.05.17.
 */
@CompileStatic
abstract class AbstractSemVerTransformer extends VersionTransformer.AbstractVersionTransformer {

    @Override
    def doTransform(@Nonnull final Object version) {
        transformSemanticVersion(version instanceof SemanticVersion ? version as SemanticVersion : SemanticVersion.parse(version as String))
    }

    /**
     * @see VersionTransformer#transform(java.lang.Object)
     */
    abstract SemanticVersion transformSemanticVersion(@Nonnull SemanticVersion semanticVersion)

}
