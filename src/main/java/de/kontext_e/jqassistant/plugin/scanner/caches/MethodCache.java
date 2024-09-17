package de.kontext_e.jqassistant.plugin.scanner.caches;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;

import java.util.HashMap;
import java.util.Map;

public class MethodCache {

    private final Map<String, MethodCoverageDescriptor> cache = new HashMap<>();
    private final Store store;


    public MethodCache(Store store) {
        this.store = store;
    }

    public MethodCoverageDescriptor create() {
        return store.create(MethodCoverageDescriptor.class);
    }

    public MethodCoverageDescriptor find(String methodName) {
        if (cache.containsKey(methodName)) {
            return cache.get(methodName);
        }
        return findInDB(methodName);
    }

    private MethodCoverageDescriptor findInDB(String methodName) {
        String query = String.format("MATCH (m:Method:Cobertura) where m.fqn = '%s' RETURN m", methodName);
        try (Result<CompositeRowObject> result = store.executeQuery(query)){
            if (result.iterator().hasNext()) {
                MethodCoverageDescriptor descriptor = result.iterator().next().get("m", MethodCoverageDescriptor.class);
                cache.put(methodName, descriptor);
                return descriptor;
            }
            return null;
        }
    }
}
