package de.kontext_e.jqassistant.plugin.scanner.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Package")
public interface PackageCoverageDescriptor extends CoberturaDescriptor{

    @Property("lineRate")
    float getLineRate();
    void setLineRate(float lineRate);

    @Property("branchRate")
    float getBranchRate();
    void setBranchRate(float branchRate);

    @Property("complexity")
    int getComplexity();
    void setComplexity(int complexity);

    @Property("name")
    String getName();
    void setName(String name);

    @Relation("HAS_CLASS")
    List<ClassCoverageDescriptor> getClasses();
    void setClasses(List<ClassCoverageDescriptor> classes);
}
