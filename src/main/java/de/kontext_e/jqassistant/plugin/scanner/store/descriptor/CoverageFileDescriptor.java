package de.kontext_e.jqassistant.plugin.scanner.store.descriptor;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

public interface CoverageFileDescriptor extends CoberturaDescriptor, FileDescriptor {

    @Relation("HAS_PACKAGE")
    List<PackageCoverageDescriptor> getPackages();
    void setPackages(List<PackageCoverageDescriptor> packages);

}
