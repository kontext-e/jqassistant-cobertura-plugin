import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import de.kontext_e.jqassistant.plugin.scanner.CoberturaCoverageScanner;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.mockito.Mockito.mock;

public class CoberturaCoveragePluginTest extends AbstractPluginIT {

    @BeforeEach
    void setUp() {
        store.beginTransaction();
    }

    @AfterEach
    void tearDown() {
        store.commitTransaction();
    }

    @Test
    void testScanner(){
        File file = new File("src/main/resources/coverage.cobertura.xml");
        CoverageReport coverageReport = CoberturaCoverageScanner.readCoverageReport(file);
        CoverageFileDescriptor coverageFileDescriptor = mock(CoverageFileDescriptor.class);
        if (coverageReport != null) {
            new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, coverageFileDescriptor);
        }
    }

}
