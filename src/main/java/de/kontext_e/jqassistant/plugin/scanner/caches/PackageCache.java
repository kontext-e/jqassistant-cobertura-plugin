package de.kontext_e.jqassistant.plugin.scanner.caches;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.buschmais.xo.api.Query.*;

public class PackageCache {

    private final Map<String, PackageCoverageDescriptor> packageCache;
    private final Store store;


    public PackageCache(Store store) {
        this.packageCache = new HashMap<>();
        this.store = store;
    }

    public PackageCoverageDescriptor create(String packageName) {
        PackageCoverageDescriptor packageCoverageDescriptor = store.create(PackageCoverageDescriptor.class);
        packageCache.put(packageName, packageCoverageDescriptor);
        return packageCoverageDescriptor;
    }

    public Optional<PackageCoverageDescriptor> find(String packageName) {
        if (packageCache.containsKey(packageName)) {
            return Optional.of(packageCache.get(packageName));
        }
        return findInDB(packageName);
    }

    private Optional<PackageCoverageDescriptor> findInDB(String packageName) {
        String query = String.format("MATCH (p:Cobertura:Package) where p.name='%s' return p", packageName);
        try (Result<CompositeRowObject> result = store.executeQuery(query)){
            if (result.iterator().hasNext()) {
                PackageCoverageDescriptor descriptor = result.iterator().next().get("p", PackageCoverageDescriptor.class);
                packageCache.put(packageName, descriptor);
                return Optional.ofNullable(descriptor);
            }
            return Optional.empty();
        }
    }
}
