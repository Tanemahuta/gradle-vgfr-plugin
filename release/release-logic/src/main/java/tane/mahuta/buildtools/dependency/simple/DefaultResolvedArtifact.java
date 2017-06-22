package tane.mahuta.buildtools.dependency.simple;

import lombok.*;
import tane.mahuta.buildtools.dependency.GAVCDescriptor;
import tane.mahuta.buildtools.dependency.ResolvedArtifact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Default implementation of {@link ResolvedArtifact}.
 *
 * @author christian.heike@icloud.com
 *         Created on 20.06.17.
 */
@Builder
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultResolvedArtifact implements ResolvedArtifact {

    @Getter(onMethod = @__({@Nonnull, @Override}))
    @NonNull
    private final GAVCDescriptor descriptor;

    @Getter(onMethod = @__({@Nullable, @Override}))
    private final File localFile;

}
