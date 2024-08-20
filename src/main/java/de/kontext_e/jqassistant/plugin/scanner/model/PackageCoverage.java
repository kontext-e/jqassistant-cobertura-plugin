package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PackageCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private float lineRate;

    @XmlAttribute(name = "branch-rate")
    private float branchRate;

    @XmlAttribute(name = "complexity")
    private int complexity;

    @XmlElement(name = "class")
    @XmlElementWrapper(name = "classes")
    private List<ClassCoverage> classes;

}
