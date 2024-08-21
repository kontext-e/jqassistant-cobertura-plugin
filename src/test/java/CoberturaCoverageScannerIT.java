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
        ClassCoverageDescriptor descriptor = store.executeQuery("MATCH (c:Cobertura:Class) where c.name = 'TestNameSpace.NormalClass' RETURN c").iterator().next().get("c", ClassCoverageDescriptor.class);

        assertThat(descriptor.getName()).isEqualTo("TestNameSpace.NormalClass");
        assertThat(descriptor.getBranchRate()).isEqualTo(1);
        assertThat(descriptor.getLineRate()).isEqualTo(1);
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

        List<String> classNames = classes.stream().map(ClassCoverageDescriptor::getName).collect(Collectors.toList());
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

        methods.forEach(m -> System.out.println(m.getName()));
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
        store.executeQuery("MATCH (c:Cobertura:Class)-[:HAS_METHOD]->(m:Cobertura:Method) where c.name='TestNameSpace.NormalClass' RETURN m").forEach(r -> methods.add(r.get("m", MethodCoverageDescriptor.class)));
        methods.forEach(m -> System.out.println(m.getName()));
        assertThat(methods.size()).isEqualTo(3);
    }

}
