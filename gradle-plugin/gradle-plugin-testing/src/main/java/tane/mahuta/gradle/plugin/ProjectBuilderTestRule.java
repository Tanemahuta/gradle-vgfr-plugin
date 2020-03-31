package tane.mahuta.gradle.plugin;

import lombok.Getter;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Optional;

/**
 * A {@link org.junit.rules.TestRule} which combines {@link TemporaryFolder} with {@link ProjectBuilder}
 *
 * @author christian.heike@icloud.com
 *         Created on 07.06.17.
 */
public class ProjectBuilderTestRule extends TemporaryFolder {

    private ProjectBuilder projectBuilder = ProjectBuilder.builder();
    private Project project;

    @Getter
    private final GradlePropertiesFile gradleProperties = new GradlePropertiesFile(this);

    /**
     * @see TemporaryFolder#TemporaryFolder()
     */
    public ProjectBuilderTestRule() {
    }

    /**
     * @see TemporaryFolder#TemporaryFolder(File)
     */
    public ProjectBuilderTestRule(File parentFolder) {
        super(parentFolder);
    }

    /**
     * @see ProjectBuilder#withGradleUserHomeDir(File)
     */
    @Incubating
    public ProjectBuilderTestRule withGradleUserHomeDir(File dir) {
        this.projectBuilder = projectBuilder.withGradleUserHomeDir(dir);
        return this;
    }

    /**
     * @see ProjectBuilder#withName(String)
     */
    public ProjectBuilderTestRule withName(String name) {
        this.projectBuilder = projectBuilder.withName(name);
        return this;
    }

    /**
     * @see ProjectBuilder#withParent(Project)
     */
    public ProjectBuilderTestRule withParent(Project parent) {
        this.projectBuilder = projectBuilder.withParent(parent);
        return this;
    }

    /**
     * @return the project ready to be used
     */
    public Project getProject() {
        return project = Optional.ofNullable(project).orElseGet(() -> projectBuilder.withProjectDir(getRoot()).withGradleUserHomeDir(new File(getRoot(), ".gradlehome")).build());
    }

}
