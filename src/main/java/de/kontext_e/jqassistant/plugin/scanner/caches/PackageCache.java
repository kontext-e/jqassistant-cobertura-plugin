package de.kontext_e.jqassistant.plugin.scanner.caches;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PackageCache {

    private final Map<String, PackageCoverageDescriptor> packageCache;
    private final Store store;


    public PackageCache(Store store) {
        this.packageCache = new HashMap<>();
        this.store = store;
    }

    public PackageCoverageDescriptor findOrCreate(String packageName) {
        Optional<PackageCoverageDescriptor> packageDescriptor = find(packageName);
        return packageDescriptor.orElseGet(() -> create(packageName));
    }

    private PackageCoverageDescriptor create(String packageName) {
        PackageCoverageDescriptor packageCoverageDescriptor = store.create(PackageCoverageDescriptor.class);
        packageCache.put(packageName, packageCoverageDescriptor);
        return packageCoverageDescriptor;
    }

    private Optional<PackageCoverageDescriptor> find(String packageName) {
        if (packageCache.containsKey(packageName)) {
            return Optional.of(packageCache.get(packageName));
        }
        return findInDB(packageName);
    }

    private Optional<PackageCoverageDescriptor> findInDB(String packageName) {
        String query = String.format("MATCH (p:Cobertura:Package) where p.name='%s' return p", packageName);
        try (Query.Result<Query.Result.CompositeRowObject> result = store.executeQuery(query)){
            if (result.iterator().hasNext()) {
                return Optional.ofNullable(result.iterator().next().get("p", PackageCoverageDescriptor.class));
            }
            return Optional.empty();
        }
    }
}
