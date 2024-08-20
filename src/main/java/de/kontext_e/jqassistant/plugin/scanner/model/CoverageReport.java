package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@XmlRootElement(name = "coverage")
@XmlAccessorType(XmlAccessType.FIELD)
public class CoverageReport {

    @XmlElement(name = "package")
    @XmlElementWrapper(name = "packages")
    private List<PackageCoverage> packages;

}
