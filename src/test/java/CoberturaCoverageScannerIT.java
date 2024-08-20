import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import de.kontext_e.jqassistant.plugin.scanner.CoberturaCoverageScanner;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.mockito.Mockito.mock;

public class CoberturaCoverageScannerIT extends AbstractPluginIT {

    @BeforeEach
    public void setUp() throws Exception {
        store.beginTransaction();
    }

    @AfterEach
    public void tearDown() throws Exception {
        store.commitTransaction();
    }

    @Test
    public void test() throws Exception {
        File file = new File("src/test/resources/coverage.cobertura_fragment1.xml");
        CoverageReport coverageReport = CoberturaCoverageScanner.readCoverageReport(file);
        CoverageFileDescriptor coverageFileDescriptor = mock(CoverageFileDescriptor.class);
        if (coverageReport != null) {
            new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, coverageFileDescriptor);
        }
    }

}
