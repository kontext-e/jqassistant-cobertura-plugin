package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.Package;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoberturaDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.model.Coverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@ScannerPlugin.Requires(FileDescriptor.class)
public class CoberturaCoverageScanner extends AbstractScannerPlugin<FileResource, CoberturaDescriptor> {

    static Logger LOGGER = LoggerFactory.getLogger(CoberturaCoverageScanner.class);
    private Store store;

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public CoberturaDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        store = scanner.getContext().getStore();
        File coverageReport = item.getFile();

        Coverage coverage = readCoverageReport(coverageReport);
        if (coverage != null) {
            saveCoverageToNeo4J(coverage);
        }
    }

    private static Coverage readCoverageReport(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(Coverage.class);
            return (Coverage) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            LOGGER.warn("Error reading coverage file", e);
            return null;
        }
    }

    private void saveCoverageToNeo4J(Coverage coverage) {
        coverage.getPackages().forEach(this::analyzePackage);
    }

    private void analyzePackage(Package packageCoverage) {
        PackageCoverageDescriptor descriptor = store.create(PackageCoverageDescriptor.class);

        descriptor.setName(packageCoverage.getName());
        descriptor.setLineRate(packageCoverage.getLineRate());
        descriptor.setBranchRate(packageCoverage.getBranchRate());
        descriptor.setComplexity(packageCoverage.getComplexity());

        packageCoverage.getClasses().forEach(this::analyzeClasses);
    }

    private void analyzeClasses(ClassCoverage className) {

    }
}
