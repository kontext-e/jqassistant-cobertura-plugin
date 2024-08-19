package de.kontext_e.jqassistant.plugin.scanner.caches;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassCache {

    public final Map<String, Map<String, ClassCoverageDescriptor>> classCache;
    private final Store store;

    public ClassCache(Store store) {
        this.store = store;
        classCache = new HashMap<>();
    }

    public ClassCoverageDescriptor findOrCreate(String className, String fileName) {
        Optional<ClassCoverageDescriptor> descriptor = find(className, fileName);
        return descriptor.orElseGet(() -> createDescriptor(className, fileName));
    }

    private ClassCoverageDescriptor createDescriptor(String className, String fileName) {
        ClassCoverageDescriptor descriptor = store.create(ClassCoverageDescriptor.class);
        Map<String, ClassCoverageDescriptor> fileToDescriptorMap = classCache.computeIfAbsent(className, k -> new HashMap<>());
        fileToDescriptorMap.put(fileName, descriptor);
        return descriptor;
    }

    private Optional<ClassCoverageDescriptor> find(String className, String fileName) {
        if (!classCache.containsKey(className)) return Optional.empty();
        if (!classCache.get(className).containsKey(fileName)) return Optional.empty();

        return Optional.of(classCache.get(className).get(fileName));
    }

}
