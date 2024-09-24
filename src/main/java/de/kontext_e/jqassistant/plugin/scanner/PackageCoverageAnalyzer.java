package de.kontext_e.jqassistant.plugin.scanner;

import de.kontext_e.jqassistant.plugin.scanner.caches.PackageCache;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.PackageCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;

public class PackageCoverageAnalyzer {
    private final PackageCache packageCache;
    private final ClassCoverageAnalyzer classCoverageAnalyzer;

    public PackageCoverageAnalyzer(PackageCache packageCache, ClassCoverageAnalyzer classCoverageAnalyzer) {
        this.packageCache = packageCache;
        this.classCoverageAnalyzer = classCoverageAnalyzer;
    }

    PackageCoverageDescriptor analyzePackage(PackageCoverage packageCoverage) {
        PackageCoverageDescriptor descriptor = packageCache
                .find(packageCoverage.getName())
                .orElseGet(() -> createPackageCoverageDescriptor(packageCoverage));

        analyseClasses(packageCoverage, descriptor);

        //Must be done after classes have been analyzed
        updateLineCoverage(descriptor);

        return descriptor;
    }

    PackageCoverageDescriptor createPackageCoverageDescriptor(PackageCoverage packageCoverage) {
        PackageCoverageDescriptor descriptor = packageCache.create(packageCoverage.getName());
        descriptor.setName(packageCoverage.getName());
        descriptor.setComplexity(packageCoverage.getComplexity());
        descriptor.setBranchRate(packageCoverage.getBranchRate());

        return descriptor;
    }

    void analyseClasses(PackageCoverage packageCoverage, PackageCoverageDescriptor descriptor) {
        for (ClassCoverage classCoverage : packageCoverage.getClasses()) {
            //Exclude compiler-generated classes
            if (classCoverage.getName().contains("$")) continue;

            ClassCoverageDescriptor classDescriptor = classCoverageAnalyzer.analyzeClass(classCoverage);
            if (descriptor.getClasses().contains(classDescriptor)) continue;
            descriptor.getClasses().add(classDescriptor);
        }
    }

    static void updateLineCoverage(PackageCoverageDescriptor descriptor) {
        long coveredLines = 0;
        long totalLines = 0;
        for (ClassCoverageDescriptor classCoverage : descriptor.getClasses()) {
            for (MethodCoverageDescriptor method : classCoverage.getMethods()) {
                coveredLines += method.getLines().stream().filter(line -> line.getHits() > 0).count();
                totalLines += method.getLines().size();
            }
        }

        descriptor.setLineRate((float) coveredLines / totalLines);
    }
}