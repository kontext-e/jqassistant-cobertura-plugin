import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import de.kontext_e.jqassistant.plugin.scanner.CoberturaCoverageScanner;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractCoberturaScannerIT extends AbstractPluginIT {

    private boolean scannerHasRun = false;

    @BeforeEach
    public void beforeEach(){
        store.beginTransaction();
        if (!scannerHasRun) {
            scanReport(store);
        }
    }

    public static void scanReport(Store store) {
        File file = new File("src/test/resources/coverage.cobertura.custom.xml");
        CoverageReport coverageReport = CoberturaCoverageScanner.readCoverageReport(file);
        CoverageFileDescriptor coverageFileDescriptor = mock(CoverageFileDescriptor.class);
        if (coverageReport != null) {
            new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, coverageFileDescriptor);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        store.commitTransaction();
    }

    @AfterAll
    public void afterAll(){
        resetStore();
    }

    private void resetStore() {
        store.start();
        store.reset();
        store.stop();
        scannerHasRun = false;
    }

}
