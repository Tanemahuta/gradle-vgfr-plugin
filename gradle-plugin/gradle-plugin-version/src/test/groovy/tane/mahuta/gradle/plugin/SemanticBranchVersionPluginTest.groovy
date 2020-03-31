package tane.mahuta.gradle.plugin

import org.eclipse.jgit.api.Git
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import tane.mahuta.buildtools.apilyzer.ApiCompatibilityReport
import tane.mahuta.buildtools.semver.DefaultSemanticVersionParser
import tane.mahuta.buildtools.semver.branch.DefaultSemanticBranchVersion
import tane.mahuta.buildtools.semver.branch.DefaultSemanticBranchVersionParser

/**
 * @author christian.heike@icloud.com
 * Created on 08.06.17.
 */
@Subject(SemanticBranchVersionPlugin)
class SemanticBranchVersionPluginTest extends Specification {

    @Rule
    @Delegate
    final ProjectBuilderTestRule projectBuilder = new ProjectBuilderTestRule()

    private Git git

    def setup() {
        git = Git.init().setDirectory(root).call()
        new File(root, "test.txt").text = "test"
        git.commit().setAll(true).setMessage("test commit").call()
        project.version = '1.0.0'
        project.apply plugin: SemanticBranchVersionPlugin
    }

    @Unroll
    def 'semantic branch version of #version in branch #branch is returns #versionString'() {
        setup:
        if (git.repository.branch != branch) {
            git.checkout().setName(branch).setCreateBranch(true).call()
        }
        project.version = version
        final semanticVersion = DefaultSemanticVersionParser.instance.parse(version, root)
        def expectedVersion = new DefaultSemanticBranchVersion(semanticVersion.major, semanticVersion.minor, semanticVersion.micro, {
            -> branchQualifier
        }, semanticVersion.qualifier)

        expect:
        project.version <=> expectedVersion == 0
        and:
        project.version as String == versionString
        and:
        project.version.branchQualifier == expectedVersion.branchQualifier

        where:
        branch              | version          | branchQualifier | versionString
        'master'            | '1.2.3'          | null            | '1.2.3'
        'master'            | '1.2.3-SNAPSHOT' | null            | '1.2.3-SNAPSHOT'
        'feature/bla/blubb' | '1.2.3'          | 'bla_blubb'     | '1.2.3-bla_blubb'
        'feature/bla/blubb' | '1.2.3-SNAPSHOT' | 'bla_blubb'     | '1.2.3-bla_blubb-SNAPSHOT'
        'support/platsch'   | '1.2.3'          | 'platsch'       | '1.2.3-platsch'
        'support/platsch'   | '1.2.3-SNAPSHOT' | 'platsch'       | '1.2.3-platsch-SNAPSHOT'
        'hotfix/1.2.3'      | '1.2.3'          | null            | '1.2.3'
        'hotfix/1.2.3'      | '1.2.3-SNAPSHOT' | null            | '1.2.3-SNAPSHOT'
        'release/1.2.3'     | '1.2.3'          | null            | '1.2.3'
        'release/1.2.3'     | '1.2.3-SNAPSHOT' | null            | '1.2.3-SNAPSHOT'
    }

    @Unroll
    def 'release transformer returns #expectedVersion for #version'() {
        setup:
        if (git.repository.branch != branch) {
            git.checkout().setName(branch).setCreateBranch(true).call()
        }
        project.version = version
        project.apply plugin: SemanticBranchVersionPlugin

        expect:
        project.extensions.versioning.releaseTransformer.apply(project.version) == DefaultSemanticBranchVersionParser.instance.parse(expectedVersion, project.projectDir)
        where:
        version          | branch          | expectedVersion
        '1.2.3-SNAPSHOT' | 'development'   | '1.2.3'
        '1.2.3'          | 'release/1.2.3' | '1.2.3'
        '1.2.3-RELEASE'  | 'release/1.2.3' | '1.2.3'
    }

    @Unroll
    def 'release transformer with report (#p/#d changes) transforms #version to #expectedVersion'() {
        setup:
        if (git.repository.branch != branch) {
            git.checkout().setName(branch).setCreateBranch(true).call()
        }
        project.version = '1.2.3-SNAPSHOT'
        project.apply plugin: SemanticVersionPlugin
        final report = Stub(ApiCompatibilityReport) {
            getDefiniteIncompatibleClasses() >> ['a'] * d
            getPossibleIncompatibleClasses() >> ['b'] * p
        }

        expect:
        project.extensions.versioning.releaseTransformerForReport.apply(DefaultSemanticBranchVersionParser.instance.parse(version, project.projectDir), report) == DefaultSemanticBranchVersionParser.instance.parse(expectedVersion, project.projectDir)

        where:
        d | p | version          | branch            | expectedVersion
        0 | 0 | '1.2.3-SNAPSHOT' | 'development'     | '1.2.4'
        1 | 0 | '1.2.3'          | 'release/1.2.3'   | '2.0.0'
        1 | 0 | '1.2.3-a'        | 'release/1.2.3-a' | '2.0.0-a'
        0 | 1 | '1.2.3'          | 'release/1.2.3'   | '1.3.0'
        1 | 1 | '1.2.3'          | 'release/1.2.3'   | '2.0.0'
    }

    @Unroll
    def 'toNextDevelopmentVersion(#version) returns #expectedVersion'() {
        setup:
        if (git.repository.branch != "development") {
            git.checkout().setName("development").setCreateBranch(true).call()
        }
        project.version = version
        project.apply plugin: SemanticBranchVersionPlugin

        expect:
        project.extensions.versioning.nextDevelopmentTransformer.apply(project.version) == DefaultSemanticBranchVersionParser.instance.parse(expectedVersion, project.projectDir)

        where:
        version          | expectedVersion
        '1.2.3-SNAPSHOT' | '1.3.0-SNAPSHOT'
        '1.2.3'          | '1.3.0-SNAPSHOT'
        '1.2.3-RELEASE'  | '1.3.0-SNAPSHOT'
        '1.2-SNAPSHOT'   | '1.3-SNAPSHOT'
        '1.2'            | '1.3-SNAPSHOT'
    }
}
