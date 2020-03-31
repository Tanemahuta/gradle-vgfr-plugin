package tane.mahuta.gradle.plugin

import org.junit.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import tane.mahuta.buildtools.apilyzer.ApiCompatibilityReport
import tane.mahuta.buildtools.semver.DefaultSemanticVersionParser

/**
 * @author christian.heike@icloud.com
 * Created on 04.06.17.
 */
@Subject(SemanticVersionPlugin)
class SemanticVersionPluginTest extends Specification {

    @Rule
    @Delegate
    final ProjectBuilderTestRule projectBuilder = new ProjectBuilderTestRule()

    def 'plugin loads version and transforms the version'() {
        setup:
        project.version = '1.2.3'
        def expectedVersion = DefaultSemanticVersionParser.instance.parse("1.2.3", null)
        final expectedSnapshotVersion = DefaultSemanticVersionParser.instance.parse("1.2.4-SNAPSHOT", null)

        when:
        project.apply plugin: SemanticVersionPlugin
        then:
        project.versioning.parser == DefaultSemanticVersionParser.instance
        and:
        project.version <=> expectedVersion == 0
        and:
        project.version == expectedVersion
        and:
        project.version as String == "1.2.3"

        when:
        project.version = "1.2.4-SNAPSHOT"
        then:
        project.version == expectedSnapshotVersion
    }

    def 'comparator works'() {
        setup:
        project.version = '1.2.3-SNAPSHOT'
        project.apply plugin: SemanticVersionPlugin

        expect:
        project.extensions.versioning.comparator.compare(
                DefaultSemanticVersionParser.instance.parse("1.0.0", project.projectDir),
                DefaultSemanticVersionParser.instance.parse("1.0.0", project.projectDir)
        ) == 0
    }

    @Unroll
    def 'release transformer returns #expectedVersion for #version'() {
        setup:
        project.version = version
        project.apply plugin: SemanticVersionPlugin

        expect:
        project.extensions.versioning.releaseTransformer.apply(project.version) == DefaultSemanticVersionParser.instance.parse(expectedVersion, project.projectDir)
        where:
        version          | expectedVersion
        '1.2.3-SNAPSHOT' | '1.2.3'
        '1.2.3'          | '1.2.3'
        '1.2.3-RELEASE'  | '1.2.3'
        '1.2-SNAPSHOT'   | '1.2'
        '1.2'            | '1.2'
    }

    @Unroll
    def 'release transformer with report (#p/#d changes) transforms #version to #expectedVersion'() {
        setup:
        project.version = '1.2.3-SNAPSHOT'
        project.apply plugin: SemanticVersionPlugin
        final report = Stub(ApiCompatibilityReport) {
            getDefiniteIncompatibleClasses() >> ['a'] * d
            getPossibleIncompatibleClasses() >> ['b'] * p
        }

        expect:
        project.extensions.versioning.releaseTransformerForReport.apply(DefaultSemanticVersionParser.instance.parse(version, project.projectDir), report) == DefaultSemanticVersionParser.instance.parse(expectedVersion, project.projectDir)

        where:
        d | p | version          | expectedVersion
        0 | 0 | '1.2.3-SNAPSHOT' | '1.2.4'
        0 | 0 | '1.2'            | '1.3'
        1 | 0 | '1.2.3'          | '2.0.0'
        1 | 0 | '1.2.3-a'        | '2.0.0-a'
        0 | 1 | '1.2.3'          | '1.3.0'
        1 | 1 | '1.2.3'          | '2.0.0'
    }

    @Unroll
    def 'toNextDevelopmentVersion(#version) returns #expectedVersion'() {
        setup:
        project.version = version
        project.apply plugin: SemanticVersionPlugin

        expect:
        project.extensions.versioning.nextDevelopmentTransformer.apply(project.version) == DefaultSemanticVersionParser.instance.parse(expectedVersion, project.projectDir)

        where:
        version          | expectedVersion
        '1.2.3-SNAPSHOT' | '1.3.0-SNAPSHOT'
        '1.2.3'          | '1.3.0-SNAPSHOT'
        '1.2.3-RELEASE'  | '1.3.0-SNAPSHOT'
        '1.2-SNAPSHOT'   | '1.3-SNAPSHOT'
        '1.2'            | '1.3-SNAPSHOT'
    }


}
