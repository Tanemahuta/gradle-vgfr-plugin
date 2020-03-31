package tane.mahuta.gradle.plugin.version

import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject
import tane.mahuta.buildtools.semver.branch.DefaultSemanticBranchVersion
import tane.mahuta.buildtools.version.VersionParser

/**
 * @author christian.heike@icloud.com
 * Created on 06.06.17.
 */
@Subject(VersioningExtension)
class VersioningExtensionTest extends Specification {

    private final Project projectMock = Mock(Project) {
        getProjectDir() >> new File('.')
    }
    private final VersioningExtension extension = new VersioningExtension(projectMock)

    def 'setting a null parser will result in a null parser'() {
        when:
        extension.parserClosure = null
        then:
        extension.parser == null
        and:
        1 * projectMock.getVersion() >> '1.2.3'
        and:
        0 * projectMock.setVersion(_)
    }

    def 'a null version will not parse the version'() {
        setup:
        final parserMock = Mock(VersionParser)
        when:
        extension.parserClosure = parserMock.&parse
        then:
        1 * projectMock.getVersion() >> null
        and:
        0 * parserMock.parse(_, _)
        and:
        0 * projectMock.setVersion(_)
    }

    def 'setting a parser will parse the version'() {
        setup:
        final parserMock = Mock(VersionParser)

        when:
        extension.parserClosure = parserMock.&parse
        then:
        1 * projectMock.getVersion() >> '1.2.3'
        and:
        1 * parserMock.parse('1.2.3', projectMock.getProjectDir()) >> '1.2.4'
        and:
        1 * projectMock.setVersion('1.2.4')
    }

    def 'setting a version with a parser parses it'() {
        setup:
        final parserMock = Mock(VersionParser)
        extension.parserClosure = parserMock.&parse

        when:
        extension.version = '1.2.5'
        then:
        1 * projectMock.setVersion('1.2.5')
        and:
        1 * projectMock.getVersion() >> '1.2.5'
        and:
        1 * parserMock.parse('1.2.5', projectMock.getProjectDir()) >> '1.2.6'
        and:
        1 * projectMock.setVersion('1.2.6')
    }

    def 'reparsing a semantic version does not use the branch qualifier'() {
        setup:
        final version = new DefaultSemanticBranchVersion(1, 2, 3, { -> "X" }, "Y")
        final parserMock = Mock(VersionParser)
        extension.parserClosure = parserMock.&parse

        when:
        extension.version = version
        then:
        1 * projectMock.setVersion(version)
        and:
        1 * projectMock.getVersion() >> version
        and:
        1 * parserMock.parse('1.2.3-Y', projectMock.getProjectDir()) >> version
        and:
        1 * projectMock.setVersion(version)
    }

}
