package de.kontext_e.jqassistant.plugin.scanner.caches;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.LineCoverageDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LineCache {

    private final Map<String, Map<Integer, LineCoverageDescriptor>> lineCache;
    private final Store store;

    public LineCache(Store store) {
        this.store = store;
        lineCache = new HashMap<>();
    }

    public LineCoverageDescriptor createDescriptor(String methodFQN, int lineNumber) {
        LineCoverageDescriptor descriptor = store.create(LineCoverageDescriptor.class);
        Map<Integer, LineCoverageDescriptor> lineNumberToDescriptorMap = lineCache.computeIfAbsent(methodFQN, k -> new HashMap<>());
        lineNumberToDescriptorMap.put(lineNumber, descriptor);
        return descriptor;
    }

    public Optional<LineCoverageDescriptor> find(String methodFQN, int lineNumber) {
        if (lineCache.containsKey(methodFQN) && lineCache.get(methodFQN).containsKey(lineNumber))
            return Optional.of(lineCache.get(methodFQN).get(lineNumber));

        return findInDB(methodFQN, lineNumber);
    }

    private Optional<LineCoverageDescriptor> findInDB(String methodFQN, int lineNumber) {
        String query = String.format("MATCH (m:Cobertura:Method)-[:HAS_LINE]->(l:Cobertura:Line) where m.fqn='%s' and l.number='%d' return l", methodFQN, lineNumber);
        try (Query.Result<Query.Result.CompositeRowObject> result = store.executeQuery(query)){
            if (result.iterator().hasNext()) {
                LineCoverageDescriptor descriptor = result.iterator().next().get("l", LineCoverageDescriptor.class);
                lineCache.computeIfAbsent(methodFQN, k -> new HashMap<>()).put(lineNumber, descriptor);
                return Optional.ofNullable(descriptor);
            }
            return Optional.empty();
        }
    }
    
}
