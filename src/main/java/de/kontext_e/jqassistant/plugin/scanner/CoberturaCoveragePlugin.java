package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ScannerPlugin.Requires(FileDescriptor.class)
public class CoberturaCoveragePlugin extends AbstractScannerPlugin<FileResource, CoberturaDescriptor> {

    static Logger LOGGER = LoggerFactory.getLogger(CoberturaCoveragePlugin.class);

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public CoberturaDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Found Cobertura report: {}", item.getFile());
        Store store = scanner.getContext().getStore();
        FileDescriptor fileDescriptor = scanner.getContext().getCurrentDescriptor();
        CoverageFileDescriptor coverageFileDescriptor = store.addDescriptorType(fileDescriptor, CoverageFileDescriptor.class);

        CoverageReport coverageReport = CoberturaCoverageScanner.readCoverageReport(item.getFile());
        if (coverageReport != null) {
            new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, coverageFileDescriptor);
            LOGGER.info("Saved Cobertura coverage report: {}", item.getFile());
        } else {
            LOGGER.warn("Error while reading Cobertura coverage report: {}, skipping ...", item.getFile());
        }

        return coverageFileDescriptor;
    }
}
