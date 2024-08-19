package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.model.PackageCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@ScannerPlugin.Requires(FileDescriptor.class)
public class CoberturaCoveragePlugin extends AbstractScannerPlugin<FileResource, CoberturaDescriptor> {

    static Logger LOGGER = LoggerFactory.getLogger(CoberturaCoveragePlugin.class);
    private Store store;

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public CoberturaDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Found Cobertura report: {}", item.getFile());
        store = scanner.getContext().getStore();
        FileDescriptor fileDescriptor = scanner.getContext().getCurrentDescriptor();
        CoverageFileDescriptor coverageFileDescriptor = store.addDescriptorType(fileDescriptor, CoverageFileDescriptor.class);

        CoverageReport coverageReport = readCoverageReport(item.getFile());
        if (coverageReport != null) {
            saveCoverageToNeo4J(coverageReport, coverageFileDescriptor);
            LOGGER.info("Saved Cobertura coverage report: {}", item.getFile());
        } else {
            LOGGER.warn("Error while reading Cobertura coverage report: {}, skipping ...", item.getFile());
        }

        return coverageFileDescriptor;
    }

    public static CoverageReport readCoverageReport(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(CoverageReport.class);
            return (CoverageReport) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            LOGGER.warn("Error reading coverage file", e);
            return null;
        }
    }

    public void saveCoverageToNeo4J(CoverageReport coverageReport, CoverageFileDescriptor coverageFileDescriptor) {
        for (PackageCoverage packageCoverage : coverageReport.getPackages()) {
            coverageFileDescriptor.getPackages().add(analyzePackage(packageCoverage));
        }
    }

    private PackageCoverageDescriptor analyzePackage(PackageCoverage packageCoverage) {
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

    private ClassCoverageDescriptor analyzeClass(ClassCoverage classCoverage) {
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

    private MethodCoverageDescriptor analyzeMethod(MethodCoverageDescriptor methodCoverageDescriptor) {


        return null;
    }
}
