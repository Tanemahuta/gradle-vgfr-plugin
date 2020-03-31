package tane.mahuta.buildtools.semver;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;
import org.apache.commons.lang.StringUtils;
import tane.mahuta.buildtools.version.SemanticVersion;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract implementation for {@link SemanticVersion}.
 *
 * @author christian.heike@icloud.com
 * Created on 11.06.17.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class AbstractSemanticVersion implements SemanticVersion {

    /**
     * The qualifier for -SNAPSHOT versions
     */
    public static final String SNAPSHOT_QUALIFIER = "SNAPSHOT";

    private static final Function<SemanticVersion, Integer>
            MAJOR_SUPPLIER = SemanticVersion::getMajor,
            MINOR_SUPPLIER = SemanticVersion::getMinor,
            MICRO_SUPPLIER = SemanticVersion::getMicro,
            SNAPSHOT_SUPPLIER = v -> v.isSnapshot() ? 0 : 1;

    /**
     * Ordered accessors for the comparison
     */
    private static final List<Function<SemanticVersion, Integer>> SUPPLIERS = Arrays.asList(
            MAJOR_SUPPLIER, MINOR_SUPPLIER, MICRO_SUPPLIER, SNAPSHOT_SUPPLIER
    );

    @Getter(onMethod = @__(@Override))
    @Wither
    protected final int major;

    @Getter(onMethod = @__(@Override))
    @Wither
    protected final int minor;

    @Getter(onMethod = @__(@Override))
    @Wither
    protected final Integer micro;

    @Override
    public final String toString() {
        return toStringRepresentation();
    }

    @Override
    public boolean isSnapshot() {
        return SNAPSHOT_QUALIFIER.equals(getQualifier());
    }

    @Override
    public int compareTo(@Nonnull final SemanticVersion o) {
        for (final Function<SemanticVersion, Integer> supplier : SUPPLIERS) {
            final Integer thisPart = Optional.ofNullable(supplier.apply(this)).orElse(0);
            final Integer otherPart = Optional.ofNullable(supplier.apply(o)).orElse(0);
            final int partRes = thisPart.compareTo(otherPart);
            if (partRes != 0) {
                return partRes;
            }
        }
        return 0;
    }

    /**
     * @return the string representation for the version
     */
    protected abstract String toStringRepresentation();

    protected static String createStringRepresentation(final int major, final int minor, final Integer micro, @Nonnull final String... qualifiers) {
        final StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor);
        Optional.ofNullable(micro).ifPresent(m -> {
            sb.append(".");
            sb.append(m);
        });
        Arrays.stream(qualifiers).filter(StringUtils::isNotBlank).forEach(q -> {
            sb.append("-");
            sb.append(q);
        });
        return sb.toString();
    }

    @Override
    public String getParseableString() {
        return createStringRepresentation(major, minor, micro, getQualifier());
    }
}
