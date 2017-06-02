package tane.mahuta.gradle.plugins.version.persistence.impl

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import tane.mahuta.gradle.plugins.version.persistence.VersionStorage
import tane.mahuta.gradle.plugins.version.persistence.VersionStorageFactory

import javax.annotation.Nonnull
import javax.annotation.Nullable
/**
 * @author christian.heike@icloud.com
 * Created on 28.05.17.
 */
@CompileStatic
@Slf4j
class ServiceLoaderVersionStorageFactory implements VersionStorageFactory {

    private final Map<String, VersionStorage> storageCache = [:]
    private final ServiceLoader<VersionStorageFactory> serviceLoader = ServiceLoader.load(VersionStorageFactory)

    @Override
    @Nullable
    VersionStorage create(@Nonnull final Project project) {
        final result = storageCache[project.path] ?: doCreate(project)
        storageCache[project.path] = result
        result
    }

    @Nullable
    VersionStorage doCreate(@Nonnull final Project project) {
        final storageFactories = getFactories()
        for (final storageFactory in storageFactories) {
            final storage = storageFactory.create(project)
            if (storage?.load() != null) {
                log.debug("Created version storage {} using: {}", storage, storageFactory)
                return storage
            }
        }
        log.debug("Could not create version storage, tried: {}", storageFactories)
        return null
    }

    protected List<VersionStorageFactory> getFactories() {
        serviceLoader.iterator() as List<VersionStorageFactory>
    }
}