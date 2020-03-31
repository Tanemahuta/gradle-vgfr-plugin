package tane.mahuta.gradle.plugin

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

/**
 * @author christian.heike@icloud.com
 * Created on 29.06.17.
 */
@Slf4j
abstract class AbstractReleasePluginIntegrationTest extends Specification {

    @Rule
    final TemporaryFolder remoteRepositoryDir = new TemporaryFolder()
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    private final StorageTransformer storage = new StorageTransformer()

    @Rule
    final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().extensions(storage))


    protected def getGradleRunner() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withDebug(true)
    }

    protected def getGradleReleaseRunner() {
        gradleRunner.withArguments('release', '--stacktrace', '--info').forwardOutput()
    }

    private Git remoteGit, git

    Git getRemoteGit() { remoteGit }

    Git getGit() { git }

    def setup() {
        remoteGit = Git.init().setBare(true).setDirectory(remoteRepositoryDir.root).call()
        git = Git.cloneRepository().setDirectory(testProjectDir.root).setURI(remoteRepositoryDir.root.toURI().toASCIIString()).call()
        log.info("Created git repository at {}", testProjectDir.root)
        new File(testProjectDir.root, ".gitignore") << """.gradle
build
.repository
"""
        commit("Initial commit")
        git.checkout().setCreateBranch(true).setName("develop").call()
        log.info("Checked out development branch.")
        final source = getResourceFile("/baseProject/build.gradle").parentFile
        FileUtils.copyDirectory(source, testProjectDir.root)
        new File(testProjectDir.root, 'gradle.properties') << "wireMockPort=${wireMockRule.port()}\n"
        commit("Initial project")
        stubFor(head(urlMatching(".+")).atPriority(2).willReturn(aResponse().withStatus(404)))
        stubFor(get(urlMatching(".+")).atPriority(2).willReturn(aResponse().withStatus(404)))
        stubFor(put(urlMatching(".+")).willReturn(aResponse().withStatus(200).withTransformers("")))
    }

    def commit(final String msg) {
        git.add().addFilepattern('.').call()
        def ref = git.commit().setAll(true).setMessage(msg).call()
        log.info("Created commit '{}': {}.", msg, ref.name())
        ref
    }

    protected File getResourceFile(final String path) {
        final url = getClass().getResource(path)
        if (url == null) {
            throw new IllegalArgumentException("Could not find resource with path: ${path}")
        }
        return new File(url.toURI())
    }

    boolean hasLocalTag(final String name) {
        git.repository.findRef("refs/tags/${name}") != null
    }

    boolean hasRemoteTag(final String name) {
        remoteGit.repository.findRef("refs/tags/${name}") != null
    }

    boolean hasLocalBranch(final String name) {
        git.repository.findRef("refs/heads/${name}") != null
    }

    boolean hasRemoteBranch(final String name) {
        remoteGit.repository.findRef("refs/heads/${name}") != null
    }

    boolean repositoryContains(final String group, final String name, final String version) {
        storage.hasContent("/${group.replace('.', '/')}/${name}/${version}/${name}-${version}.jar")
    }

    File getImplementationFile() {
        new File(testProjectDir.root, "impl/src/main/java/com/example/SomeImplementation.java")
    }

    File getApiFile() {
        new File(testProjectDir.root, "api/src/main/java/com/example/SomeInterface.java")
    }

    File getGradlePropertiesFile() {
        new File(testProjectDir.root, "gradle.properties")
    }

    Properties getGradleProperties() {
        final Properties result = new Properties()
        new File(testProjectDir.root, "gradle.properties").withReader { result.load(it) }
        return result
    }

}
