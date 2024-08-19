package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.model.PackageCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.File;

public class CoberturaCoverageScanner {
    public final Store store;

    public CoberturaCoverageScanner(Store store) {
        this.store = store;
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
            coverageFileDescriptor.getPackages().add(analyzePackage(packageCoverage));
        }
    }

    public PackageCoverageDescriptor analyzePackage(PackageCoverage packageCoverage) {
        PackageCoverageDescriptor descriptor = store.create(PackageCoverageDescriptor.class);

        descriptor.setName(packageCoverage.getName());
        descriptor.setLineRate(packageCoverage.getLineRate());
        descriptor.setBranchRate(packageCoverage.getBranchRate());
        descriptor.setComplexity(packageCoverage.getComplexity());

        for (ClassCoverage classCoverage : packageCoverage.getClasses()) {
            //Exclude compiler-generated classes
            if (classCoverage.getName().contains("$")) continue;

            descriptor.getClasses().add(analyzeClass(classCoverage));
        }

        return descriptor;
    }

    public ClassCoverageDescriptor analyzeClass(ClassCoverage classCoverage) {
        //TODO See what "CleanUpRegex" is all about

        ClassCoverageDescriptor descriptor = store.create(ClassCoverageDescriptor.class);

        descriptor.setName(classCoverage.getName());
        descriptor.setLineRate(classCoverage.getLineRate());
        descriptor.setBranchRate(classCoverage.getBranchRate());
        descriptor.setComplexity(classCoverage.getComplexity());
        descriptor.setFileName(classCoverage.getFileName());

        for (MethodCoverageDescriptor methodCoverageDescriptor : descriptor.getMethods()) {
            MethodCoverageDescriptor methodDescriptor = analyzeMethod(methodCoverageDescriptor);
            descriptor.getMethods().add(methodDescriptor);
        }

        return descriptor;
    }

    public MethodCoverageDescriptor analyzeMethod(MethodCoverageDescriptor methodCoverageDescriptor) {


        return null;
    }
}