package tane.mahuta.buildtools.version;

/**
 * Interface for a semantic version.
 *
 * @author christian.heike@icloud.com
 *         Created on 06.06.17.
 */
public interface SemanticVersion extends Comparable<SemanticVersion> {

    /**
     * @return major version part
     */
    int getMajor();

    /**
     * @return the minor version part
     */
    int getMinor();

    /**
     * @return the micro version part (optional)
     */
    Integer getMicro();

    /**
     * @return the qualifier for the version (optional)
     */
    String getQualifier();

    /**
     * @return {@code true} if this denotes a snapshot version
     */
    boolean isSnapshot();

    /**
     * @return the full version with qualifier
     */
    String getParseableString();
}
