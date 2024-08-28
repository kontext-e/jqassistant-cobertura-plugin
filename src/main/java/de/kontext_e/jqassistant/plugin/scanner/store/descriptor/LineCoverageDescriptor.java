package de.kontext_e.jqassistant.plugin.scanner.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Line")
public interface LineCoverageDescriptor extends CoberturaDescriptor {

    @Property("number")
    int getNumber();
    void setNumber(int number);

    @Property("hits")
    int getHits();
    void setHits(int hits);

}
