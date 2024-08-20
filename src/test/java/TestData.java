import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.model.MethodCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.PackageCoverage;

import java.util.List;

public class TestData {

    private static int someInt(){
        return (int) ((Math.random() * 100) + 2);
    }

    private static float someFloat(){
        return (float) Math.random();
    }

    protected static CoverageReport someCoverageReport(PackageCoverage... packages){
        return CoverageReport.builder().packages(List.of(packages)).build();
    }

    protected static PackageCoverage somePackageCoverage(ClassCoverage... classCoverages) {
        return PackageCoverage.builder()
                .classes(List.of(classCoverages))
                .name("Package" + someInt())
                .complexity(someInt())
                .lineRate(someFloat())
                .branchRate(someFloat())
                .build();
    }

    protected static ClassCoverage someClassCoverage(MethodCoverage... methodCoverages) {
        return ClassCoverage.builder()
                .methods(List.of(methodCoverages))
                .name("Class" + someInt())
                .complexity(someInt())
                .lineRate(someFloat())
                .branchRate(someFloat()).build();
    }

    protected static MethodCoverage someMethodCoverage() {
        return MethodCoverage.builder()
                .branchRate(someFloat())
                .lineRate(someFloat())
                .complexity(someInt())
                .name("Method" + someInt())
                .signature("Signature" + someInt())
                .build();
    }

    protected static CoverageReport someCoverageReport(ClassCoverage classCoverage) {
        return CoverageReport.builder()
                .packages(List.of(
                        somePackageCoverage(classCoverage))
                ).build();
    }

    protected static CoverageReport someCoverageReport(MethodCoverage methodCoverage) {
        return CoverageReport.builder()
                .packages(List.of(
                        somePackageCoverage(someClassCoverage(methodCoverage))
                )).build();
    }

}
