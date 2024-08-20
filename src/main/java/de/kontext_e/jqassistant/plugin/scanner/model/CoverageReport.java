package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "coverage")
@XmlAccessorType(XmlAccessType.FIELD)
public class CoverageReport {

    @XmlElement(name = "package")
    @XmlElementWrapper(name = "packages")
    private List<PackageCoverage> packages;

}
