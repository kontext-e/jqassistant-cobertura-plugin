import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class CoberturaCoverageScannerIT extends AbstractCoberturaScannerIT {

    @Test
    @TestStore
    void testPackages() {
        List<PackageCoverageDescriptor> packages = new LinkedList<>();
        store.executeQuery("MATCH (p:Cobertura:Package) return p").forEach(r -> packages.add(r.get("p", PackageCoverageDescriptor.class)));
        assertThat(packages.size()).isEqualTo(2);
    }

    @Test
    @TestStore
    void testRelationOfPackageToClass() {
        List<String> classes = new LinkedList<>();
        store.executeQuery("MATCH (p:Cobertura:Package)-[:HAS_CLASS]->(c:Cobertura:Class) RETURN c.name").forEach(r -> classes.add(r.get("c", String.class)));
        assertThat(classes.size()).isEqualTo(5);
    }

    @Test
    @TestStore
    void testClassMetadata(){
        ClassCoverageDescriptor descriptor = store.executeQuery("MATCH (c:Cobertura:Class) where c.fqn = 'TestNameSpace.NormalClass' RETURN c").iterator().next().get("c", ClassCoverageDescriptor.class);

        assertThat(descriptor.getFqn()).isEqualTo("TestNameSpace.NormalClass");
        assertThat(descriptor.getName()).isEqualTo("NormalClass");
//WIP        assertThat(descriptor.getBranchRate()).isEqualTo(1);
        assertThat(descriptor.getLineRate()).isEqualTo(0.7777778f);
        assertThat(descriptor.getFileName()).isEqualTo("TestDirectory/TestFileTwo.cs");
        assertThat(descriptor.getComplexity()).isEqualTo(29);
        assertThat(descriptor.getFirstLine()).isEqualTo(14);
        assertThat(descriptor.getLastLine()).isEqualTo(78);
    }

    @Test
    @TestStore
    void testCorrectClassnames() {
        List<ClassCoverageDescriptor> classes = new LinkedList<>();
        store.executeQuery("MATCH (c:Cobertura:Class) return c").forEach(r -> classes.add(r.get("c", ClassCoverageDescriptor.class)));

        List<String> classNames = classes.stream().map(ClassCoverageDescriptor::getFqn).collect(Collectors.toList());
        assertThat(classNames.size()).isEqualTo(5);
        assertThat(classNames.contains("TestNameSpace.NormalClass")).isTrue();
        assertThat(classNames.contains("TestNameSpace.ClassWithLocalMethod")).isTrue();
        assertThat(classNames.contains("TestNameSpace.ClassWithAsyncMethods")).isTrue();
        assertThat(classNames.contains("TestNameSpace.GenericClass")).isTrue();
        assertThat(classNames.contains("TestNameSpace.ClassWithNestedClass")).isTrue();
    }

    @Test
    @TestStore
    void testCorrectMethodNames() {
        List<MethodCoverageDescriptor> methods = new LinkedList<>();
        store.executeQuery("MATCH (m:Cobertura:Method) return m").forEach(r-> methods.add(r.get("m", MethodCoverageDescriptor.class)));

        List<String> methodNames = methods.stream().map(MethodCoverageDescriptor::getName).collect(Collectors.toList());
        assertThat(methodNames.size()).isEqualTo(7);
        assertThat(methodNames.contains("MethodOne")).isTrue();
        assertThat(methodNames.contains("MethodTwo")).isTrue();
        assertThat(methodNames.contains("localMethod")).isTrue();
        assertThat(methodNames.contains("LocalMethodName")).isTrue();
        assertThat(methodNames.stream().filter(n -> n.equals(".ctor")).count()).isEqualTo(2);
        assertThat(methodNames.stream().filter(n -> n.equals("AsyncMethod")).count()).isEqualTo(1);
    }

    @Test
    @TestStore
    void testMethodMetadata(){
        MethodCoverageDescriptor descriptor = store.executeQuery("MATCH (m:Cobertura:Method) WHERE m.name='MethodOne' RETURN m").iterator().next().get("m", MethodCoverageDescriptor.class);
        assertThat(descriptor.getName()).isEqualTo("MethodOne");
        assertThat(descriptor.getBranchRate()).isEqualTo(1);
        assertThat(descriptor.getLineRate()).isEqualTo(1);
        assertThat(descriptor.getComplexity()).isEqualTo(10);
        assertThat(descriptor.getFirstLine()).isEqualTo(36);
        assertThat(descriptor.getLastLine()).isEqualTo(59);
        assertThat(descriptor.getSignature()).isEqualTo("(VariableOne,Variable2)");
    }

    @Test
    @TestStore
    void testRelationOfClassToMethod(){
        List<MethodCoverageDescriptor> methods = new LinkedList<>();
        store.executeQuery("MATCH (c:Cobertura:Class)-[:HAS_METHOD]->(m:Cobertura:Method) where c.fqn='TestNameSpace.NormalClass' RETURN m").forEach(r -> methods.add(r.get("m", MethodCoverageDescriptor.class)));
        assertThat(methods.size()).isEqualTo(3);
    }

    @Test
    @TestStore
    void testCalculationOfMethodCoverageFullCoverage(){
        List<MethodCoverageDescriptor> methods = new LinkedList<>();
        store.executeQuery("MATCH (m:Cobertura:Method) WHERE m.name='MethodOne' RETURN m").forEach(r -> methods.add(r.get("m", MethodCoverageDescriptor.class)));
        assertThat(methods.size()).isEqualTo(1);
        assertThat(methods.get(0).getLineRate()).isEqualTo(1f);
    }

    @Test
    @TestStore
    void testCalculationOfMethodCoverageSomeCoverage(){
        List<MethodCoverageDescriptor> methods = new LinkedList<>();
        store.executeQuery("MATCH (m:Cobertura:Method) WHERE m.name='MethodTwo' RETURN m").forEach(r -> methods.add(r.get("m", MethodCoverageDescriptor.class)));
        assertThat(methods.size()).isEqualTo(1);
        assertThat(methods.get(0).getLineRate()).isEqualTo(0.75f);
    }

    @Test
    @TestStore
    void testCalculationOfMethodCoverageNoCoverage(){
        List<MethodCoverageDescriptor> methods = new LinkedList<>();
        store.executeQuery("MATCH (m:Cobertura:Method) WHERE m.fqn='TestNameSpace.NormalClass..ctor' RETURN m").forEach(r -> methods.add(r.get("m", MethodCoverageDescriptor.class)));
        assertThat(methods.size()).isEqualTo(1);
        assertThat(methods.get(0).getLineRate()).isEqualTo(0f);
    }

    @Test
    @TestStore
    void testClassLineCoverage() {
        List<ClassCoverageDescriptor> classLineCoverage = new LinkedList<>();
        store.executeQuery("MATCH (c:Cobertura:Class) WHERE c.name = 'NormalClass' RETURN c").forEach(r -> classLineCoverage.add(r.get("c", ClassCoverageDescriptor.class)));
        assertThat(classLineCoverage.size()).isEqualTo(1);
        assertThat(classLineCoverage.get(0).getLineRate()).isEqualTo(0.777777777f);
    }

    @Test
    @TestStore
    void testPackageLineCoverage(){
        List<PackageCoverageDescriptor> packageLineCoverage = new LinkedList<>();
        store.executeQuery("MATCH (p:Cobertura:Package) WHERE p.name = 'TestNameSpace' RETURN p").forEach(r -> packageLineCoverage.add(r.get("p", PackageCoverageDescriptor.class)));
        assertThat(packageLineCoverage.size()).isEqualTo(1);
        assertThat(packageLineCoverage.get(0).getLineRate()).isEqualTo(0.6760563f);
    }
}
