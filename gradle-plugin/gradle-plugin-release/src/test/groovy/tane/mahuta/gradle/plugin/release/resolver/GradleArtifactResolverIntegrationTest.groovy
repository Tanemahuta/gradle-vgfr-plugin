package tane.mahuta.gradle.plugin.release.resolver

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import tane.mahuta.buildtools.dependency.simple.DefaultGAVCDescriptor
import tane.mahuta.gradle.plugin.ProjectBuilderTestRule

/**
 * @author christian.heike@icloud.com
 * Created on 22.06.17.
 */
@Subject(GradleArtifactResolver)
class GradleArtifactResolverIntegrationTest extends Specification {

    @Rule
    @Delegate
    final ProjectBuilderTestRule projectBuilder = new ProjectBuilderTestRule()

    @Rule
    final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().usingFilesUnderDirectory(new File("src/test/resources/").absolutePath))

    private GradleArtifactResolver resolver

    def setup() {
        project.repositories {
            maven { url "http://localhost:${wireMockRule.port()}/maven2/" }
        }
        resolver = new GradleArtifactResolver(project)
    }

    protected redownloadMappings() {
        new File("src/test/resources/mappings/").eachFile { f ->
            if (!['GET', 'HEAD'].any { f.name.endsWith("-${it}.json") }) {
                def m = f.text =~ /"bodyFileName" : "([^"]+)",/
                m.find()
                final fn = m.group(1)
                m = f.text =~ /"url" : "([^"]+)",/
                m.find()
                final url = "http://repo1.maven.org${m.group(1)}"
                if (f.text.contains('"status" : 200,')) {
                    new URL(url).withInputStream { is ->
                        new File(f.parentFile, "../__files/${fn}").withOutputStream { os ->
                            os << is
                        }
                    }
                }
                if (f.text.contains('"HEAD"')) {
                    new File(f.absolutePath.replace('.json', '-HEAD.json')).text = f.text.replaceAll(/"bodyFileName" : "([^"]+)",/, '')
                    new File(f.absolutePath.replace('.json', '-GET.json')).text = f.text.replace('"HEAD"', '"GET"')
                    f.delete()
                } else if (f.text.contains('"GET"')) {
                    new File(f.absolutePath.replace('.json', '-HEAD.json')).text = f.text.replace('"GET"', '"HEAD"').replaceAll(/"bodyFileName" : "([^"]+)",/, '')
                    new File(f.absolutePath.replace('.json', '-GET.json')).text = f.text
                    f.delete()
                }
            }
        }
    }

    @Unroll
    def 'latest release of #group:#artifact:#version = #expectedVersion (#expectedDependencies dependencies)'() {
        setup:
        final currentDescriptor = DefaultGAVCDescriptor.builder().group(group).artifact(artifact).version(version).build()
        final expectedDescriptor = DefaultGAVCDescriptor.builder().group(group).artifact(artifact).version(expectedVersion).build()

        when:
        final actualDescriptor = resolver.resolveLastReleaseArtifact(currentDescriptor)

        then:
        actualDescriptor != null
        actualDescriptor.descriptor.group == expectedDescriptor.group
        actualDescriptor.descriptor.artifact == expectedDescriptor.artifact
        actualDescriptor.descriptor.version == expectedDescriptor.version
        actualDescriptor.descriptor.classifier == expectedDescriptor.classifier
        actualDescriptor.classpathDependencies.size() == expectedDependencies

        where:
        group              | artifact           | version                | expectedVersion        | expectedDependencies
        'commons-io'       | 'commons-io'       | '2.4'                  | '2.5'                  | 0
        'commons-io'       | 'commons-io'       | '1.8-SNAPSHOT'         | '1.4'                  | 0
        'junit'            | 'junit'            | '4.14-SNAPSHOT'        | '4.12'                 | 1
        'junit'            | 'junit'            | '5.0'                  | '4.12'                 | 1
        'org.json'         | 'json'             | '20160810'             | '20170516'             | 0
        'org.eclipse.jgit' | 'org.eclipse.jgit' | '4.7.0.201704051617-r' | '4.7.1.201706071930-r' | 7
        'com.hazelcast'    | 'hazelcast-client' | '3.8.4-SNAPSHOT'       | '3.8.3'                | 1
    }

    def 'latest release for not available returns null'() {
        expect:
        resolver.resolveLastReleaseArtifact(
                DefaultGAVCDescriptor.builder().group('unknown').artifact('unknown').version('1.0.0').build()
        ) == null
    }

}
