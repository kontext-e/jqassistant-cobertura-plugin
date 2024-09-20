package de.kontext_e.jqassistant.plugin.scanner;

import de.kontext_e.jqassistant.plugin.scanner.caches.LineCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.MethodCache;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.LineCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.MethodCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.LineCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;

import java.util.Optional;

import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseClassName;
import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseMethodName;

public class MethodCoverageAnalyzer {

    private final MethodCache methodCache;
    private final LineCache lineCache;

    public MethodCoverageAnalyzer(MethodCache methodCache, LineCache lineCache) {
        this.methodCache = methodCache;
        this.lineCache = lineCache;
    }

    MethodCoverageDescriptor analyzeMethod(MethodCoverage methodCoverage, ClassCoverage classCoverage) {
        String className = parseClassName(classCoverage.getName());
        String methodName = parseMethodName(methodCoverage.getName(), classCoverage.getName());
        String fqn = className + "." + methodName;

        MethodCoverageDescriptor methodDescriptor = methodCache.find(fqn).orElseGet(() -> createMethodCoverageDescriptor(methodCoverage, methodName, fqn));

        for (LineCoverage lineCoverage : methodCoverage.getLines()) {
            LineCoverageDescriptor lineCoverageDescriptor = analyzeLine(lineCoverage, fqn);
            if (methodDescriptor.getLines().contains(lineCoverageDescriptor)) continue;
            methodDescriptor.getLines().add(lineCoverageDescriptor);
        }

        //FIXME "EMS.APIService.Persistence.Context.DataContext.OnModelCreating"
        //Must be done after Lines have been analyzed
        long coveredLines = methodDescriptor.getLines().stream().filter(line -> line.getHits() > 0).count();
        long totalLines = methodDescriptor.getLines().size();
        methodDescriptor.setLineRate((float) coveredLines / totalLines);

        return methodDescriptor;
    }

    private MethodCoverageDescriptor createMethodCoverageDescriptor(MethodCoverage methodCoverage, String methodName, String fqn) {
        MethodCoverageDescriptor descriptor = methodCache.create();
        descriptor.setName(methodName);
        descriptor.setFqn(fqn);
        descriptor.setBranchRate(methodCoverage.getBranchRate());
        descriptor.setComplexity(methodCoverage.getComplexity());
        descriptor.setSignature(methodCoverage.getSignature());
        descriptor.setFirstLine(methodCoverage.getFirstLine());
        descriptor.setLastLine(methodCoverage.getLastLine());

        return descriptor;
    }

    private LineCoverageDescriptor analyzeLine(LineCoverage lineCoverage, String fqn) {
        Optional<LineCoverageDescriptor> descriptor = lineCache.find(fqn, lineCoverage.getNumber());

        LineCoverageDescriptor lineCoverageDescriptor;
        if (descriptor.isPresent()) {
            lineCoverageDescriptor = descriptor.get();
            lineCoverageDescriptor.setHits(lineCoverageDescriptor.getHits() + lineCoverage.getHits());
        } else {
            lineCoverageDescriptor = lineCache.createDescriptor(fqn, lineCoverage.getNumber());
            lineCoverageDescriptor.setNumber(lineCoverage.getNumber());
            lineCoverageDescriptor.setHits(lineCoverage.getHits());
        }
        return lineCoverageDescriptor;
    }

}
