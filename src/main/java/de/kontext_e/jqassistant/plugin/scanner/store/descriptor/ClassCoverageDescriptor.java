package de.kontext_e.jqassistant.plugin.scanner.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Class")
public interface ClassCoverageDescriptor extends CoberturaDescriptor {

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

    @Property("fileName")
    String getFileName();
    void setFileName(String fileName);

    @Relation("HAS_METHOD")
    List<MethodCoverageDescriptor> getMethods();
    void setMethods(List<MethodCoverageDescriptor> methods);

    @Property("firstLine")
    int getFirstLine();
    void setFirstLine(int firstLine);

    @Property("lastLine")
    int getLastLine();
    void setLastLine(int lastLine);
}
