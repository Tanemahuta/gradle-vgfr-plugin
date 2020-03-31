package tane.mahuta.buildtools.vcs.jgit

import com.atlassian.jgitflow.core.InitContext
import com.atlassian.jgitflow.core.JGitFlow
import com.atlassian.jgitflow.core.JGitFlowWithConfig
import com.atlassian.jgitflow.core.exception.JGitFlowException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author christian.heike@icloud.com
 * Created on 07.06.17.
 */
@Subject(JGitFlowAccessor)
class JGitFlowAccessorTest extends Specification {

    private static final InitContext INIT_CTX = new InitContext()
            .setDevelop("develop")
            .setMaster("master")
            .setRelease("release/")
            .setHotfix("hotfix/")
            .setSupport("support/")
            .setVersiontag("version/")

    @Rule
    final TemporaryFolder folder1 = new TemporaryFolder()
    @Rule
    final TemporaryFolder folder2 = new TemporaryFolder()

    private JGitFlow jGitFlow
    private JGitFlowAccessor accessor
    private String head
    private Git remote

    def setup() {
        remote = Git.init().setBare(true).setDirectory(folder2.root).call()
        final git = Git.cloneRepository().setDirectory(folder1.root).setURI(folder2.root.toURI().toASCIIString()).call()
        jGitFlow = new JGitFlowWithConfig(git, INIT_CTX)
        new File(folder1.getRoot(), "test.txt").createNewFile()
        accessor = new JGitFlowAccessor(jGitFlow)
        jGitFlow.git().add().addFilepattern("test.txt").call()
        head = jGitFlow.git().commit().setMessage("Test message").call().name() as String
        jGitFlow.git().checkout().setCreateBranch(true).setName("develop").call()
    }

    def "getBranch returns checked out branch"() {
        when:
        jGitFlow.git().checkout().setName("test").setCreateBranch(true).call()

        then:
        accessor.branch == "test"
    }

    def "getRevisionId returns head commit id"() {
        expect:
        accessor.revisionId == head
    }

    def 'getUncommittedFilePaths works'() {
        expect:
        accessor.uncommittedFilePaths.isEmpty()

        when:
        new File(folder1.getRoot(), "test.txt") << "changed"
        new File(folder1.getRoot(), "test2.txt").createNewFile()
        then:
        accessor.uncommittedFilePaths == ['test.txt', 'test2.txt'] as Set
    }

    @Unroll
    def "flowConfig changes configuration for #property from #initialValue to #newValue"() {
        setup:
        final flowConfig = accessor.flowConfig

        expect:
        flowConfig[property] == initialValue

        when:
        flowConfig[property] = newValue
        then:
        flowConfig[property] == newValue

        where:
        property              | initialValue        | newValue
        'productionBranch'    | INIT_CTX.master     | 'myMaster'
        'developmentBranch'   | INIT_CTX.develop    | 'myDevelop'
        'featureBranchPrefix' | INIT_CTX.feature    | 'myFeature/'
        'releaseBranchPrefix' | INIT_CTX.release    | 'myRelease/'
        'hotfixBranchPrefix'  | INIT_CTX.hotfix     | 'myHotfix/'
        'supportBranchPrefix' | INIT_CTX.support    | 'support/'
        'versionTagPrefix'    | INIT_CTX.versiontag | 'myVersion/'
    }

    @Unroll
    def "start on #startBranch ends on #expectedReleaseBranch and finish ends on #expectedFinishBranch"() {
        setup:
        if (accessor.branch != startBranch) {
            jGitFlow.git().checkout().setCreateBranch(true).setName(startBranch).call()
        }

        when:
        accessor.startReleaseBranch("1.2.3")
        then:
        accessor.branch == expectedReleaseBranch
        when:
        new File(folder1.root, "xy.txt") << "Text"
        accessor.commitFiles("Changing a file")
        final head = accessor.revisionId
        accessor.finishReleaseBranch("1.2.3")

        then:
        accessor.branch == expectedFinishBranch
        jGitFlow.git().repository.findRef("refs/heads/${expectedReleaseBranch}") == null
        remote.repository.findRef("refs/heads/${expectedReleaseBranch}") == null
        and:
        isMerged(head)

        where:
        startBranch     | expectedReleaseBranch | expectedFinishBranch
        'release/1.2.3' | 'release/1.2.3'       | INIT_CTX.getDevelop()
        'development'   | 'release/1.2.3'       | INIT_CTX.getDevelop()
        'hotfix/1.2.3'  | 'hotfix/1.2.3'        | INIT_CTX.getDevelop()
        'support/1.2.3' | 'release/1.2.3'       | INIT_CTX.getDevelop()
        'feature/xy'    | 'release/1.2.3'       | INIT_CTX.getDevelop()
    }

    def "finish on other branches result in error"() {
        setup:
        jGitFlow.git().checkout().setCreateBranch(true).setName("bla/blubb").call()

        expect:
        accessor.finishReleaseBranch("1.2.3") == false
    }

    def "finish with merge problems in master and development results in error"() {
        when:
        accessor.startReleaseBranch("1.2.3")
        and: 'Producing a merge commit'
        new File(folder1.root, "xy.txt") << "Some commit"
        accessor.commitFiles("Commit on release branch")
        accessor.checkout(INIT_CTX.develop)
        new File(folder1.root, "xy.txt") << "Some develop commit"
        accessor.commitFiles("Commit on development branch")
        accessor.checkout(INIT_CTX.master)
        new File(folder1.root, "xy.txt") << "Some master commit"
        accessor.commitFiles("Commit on master branch")
        accessor.checkout("release/1.2.3")

        and:
        accessor.finishReleaseBranch("1.2.3")
        then:
        thrown(JGitFlowException)
    }

    def "push pushes branch"() {
        setup:
        jGitFlow.git().checkout().setCreateBranch(true).setName("xy").call()

        when:
        accessor.push()
        then:
        remote.repository.allRefs.keySet() == ['refs/heads/xy'] as Set
    }

    def 'pushes tags'() {
        setup:
        jGitFlow.git().tag().setMessage("xy").setName("xy").call()

        when:
        accessor.pushTags()

        then:
        remote.repository.findRef("refs/tags/xy") != null
    }

    private boolean isMerged(final String commitId) {
        def revWalk
        try {
            final repo = jGitFlow.git().repository
            revWalk = new RevWalk(repo)
            final currentCommit = revWalk.parseCommit(repo.resolve(accessor.revisionId))
            final questionableCommit = revWalk.parseCommit(repo.resolve(commitId))
            return revWalk.isMergedInto(questionableCommit, currentCommit)
        } finally {
            revWalk?.close()
        }
    }

}
