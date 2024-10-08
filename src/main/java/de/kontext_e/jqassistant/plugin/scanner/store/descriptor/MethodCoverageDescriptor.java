package de.kontext_e.jqassistant.plugin.scanner.store.descriptor;


import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Method")
public interface MethodCoverageDescriptor extends CoberturaDescriptor{

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

    @Property("fqn")
    String getFqn();
    void setFqn(String fqn);

    @Property("signature")
    String getSignature();
    void setSignature(String signature);

    @Property("firstLine")
    int getFirstLine();
    void setFirstLine(int firstLine);

    @Property("lastLine")
    int getLastLine();
    void setLastLine(int lastLine);

    @Relation("HAS_LINE")
    List<LineCoverageDescriptor> getLines();
    void setLines(List<LineCoverageDescriptor> lines);

}
