import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.CoberturaCoverageScanner;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.model.MethodCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class CoberturaCoverageScannerTest {

    private Store store;

    @BeforeEach
    void setUp() {
        store = getMockStore();
    }

    private static Store getMockStore() {
        Store mockStore = mock(Store.class);
        when(mockStore.create(CoverageFileDescriptor.class)).thenReturn(mock(CoverageFileDescriptor.class));
        when(mockStore.create(PackageCoverageDescriptor.class)).thenReturn(mock(PackageCoverageDescriptor.class));
        when(mockStore.create(ClassCoverageDescriptor.class)).thenReturn(mock(ClassCoverageDescriptor.class));
        when(mockStore.create(MethodCoverageDescriptor.class)).thenReturn(mock(MethodCoverageDescriptor.class));
        return mockStore;
    }

    @Test
    void testNoPackages(){
        CoverageReport coverageReport = TestData.someCoverageReport();

        Store mockStore = getMockStore();
        new CoberturaCoverageScanner(mockStore).saveCoverageToNeo4J(coverageReport, mock(CoverageFileDescriptor.class));

        verify(mockStore, never()).create(PackageCoverageDescriptor.class);
    }

    @Test
    void testClasses(){
        CoverageReport coverageReport = TestData.someCoverageReport(
                TestData.somePackageCoverage(),
                TestData.somePackageCoverage(
                        TestData.someClassCoverage(),
                        TestData.someClassCoverage(
                                TestData.someMethodCoverage(),
                                TestData.someMethodCoverage()
                        )
                )
        );

        new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, mock(CoverageFileDescriptor.class));

        verify(store, times(2)).create(PackageCoverageDescriptor.class);
        verify(store, times(2)).create(ClassCoverageDescriptor.class);
        verify(store, times(2)).create(MethodCoverageDescriptor.class);
    }

    @Test
    void testCompilerGeneratedClass(){
        ClassCoverage classCoverage = TestData.someClassCoverage();
        classCoverage.setName("$ClassToBeOmitted");
        CoverageReport coverageReport = TestData.someCoverageReport(classCoverage);

        new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, mock(CoverageFileDescriptor.class));

        verify(store, never()).create(ClassCoverageDescriptor.class);
    }

    @Test
    void testLocalMethodName(){
        MethodCoverage methodCoverage = TestData.someMethodCoverage();
        methodCoverage.setName("<AddCalenderEntriesToContractItem>g__calendarEntryInRequestedTimeFrame|0");
        CoverageReport coverageReport = TestData.someCoverageReport(methodCoverage);

        MethodCoverageDescriptor descriptor = mock(MethodCoverageDescriptor.class);
        when(store.create(MethodCoverageDescriptor.class)).thenReturn(descriptor);

        new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, mock(CoverageFileDescriptor.class));

        verify(descriptor).setName("calendarEntryInRequestedTimeFrame");
    }

    @Test
    void testAsyncMethodName(){
        MethodCoverage methodCoverage = TestData.someMethodCoverage();
        methodCoverage.setName("MoveNext");
        ClassCoverage classCoverage = TestData.someClassCoverage(methodCoverage);
        classCoverage.setName("OuterClass/<AsyncMethod>d__6");
        classCoverage.setMethods(List.of(methodCoverage));
        CoverageReport coverageReport = TestData.someCoverageReport(classCoverage);

        MethodCoverageDescriptor descriptor = mock(MethodCoverageDescriptor.class);
        when(store.create(MethodCoverageDescriptor.class)).thenReturn(descriptor);

        new CoberturaCoverageScanner(store).saveCoverageToNeo4J(coverageReport, mock(CoverageFileDescriptor.class));

        verify(descriptor).setName("AsyncMethod");
    }

}
