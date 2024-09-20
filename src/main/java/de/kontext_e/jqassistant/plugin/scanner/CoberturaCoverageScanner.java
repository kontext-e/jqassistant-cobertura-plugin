package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.caches.ClassCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.LineCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.MethodCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.PackageCache;
import de.kontext_e.jqassistant.plugin.scanner.model.*;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.File;

public class CoberturaCoverageScanner {

    private final PackageCoverageAnalyzer packageCoverageAnalyzer;

    public CoberturaCoverageScanner(Store store) {
        PackageCache packageCache = new PackageCache(store);
        ClassCache classCache = new ClassCache(store);
        MethodCache methodCache = new MethodCache(store);
        LineCache lineCache = new LineCache(store);

        MethodCoverageAnalyzer methodCoverageAnalyzer = new MethodCoverageAnalyzer(methodCache, lineCache);
        ClassCoverageAnalyzer classCoverageAnalyzer = new ClassCoverageAnalyzer(classCache, methodCoverageAnalyzer);
        packageCoverageAnalyzer = new PackageCoverageAnalyzer(packageCache, classCoverageAnalyzer);
    }

    public static CoverageReport readCoverageReport(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(CoverageReport.class);
            return (CoverageReport) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            CoberturaCoveragePlugin.LOGGER.warn("Error reading coverage file", e);
            return null;
        }
    }

    public void saveCoverageToNeo4J(CoverageReport coverageReport, CoverageFileDescriptor coverageFileDescriptor) {
        for (PackageCoverage packageCoverage : coverageReport.getPackages()) {
            coverageFileDescriptor.getPackages().add(packageCoverageAnalyzer.analyzePackage(packageCoverage));
        }
    }
}
