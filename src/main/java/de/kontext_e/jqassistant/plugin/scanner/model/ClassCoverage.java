package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private float lineRate;

    @XmlAttribute(name = "branch-rate")
    private float branchRate;

    @XmlAttribute(name = "complexity")
    private int complexity;

    @XmlAttribute(name = "fileName")
    private String fileName;

    @XmlElement(name = "method")
    @XmlElementWrapper(name = "methods")
    private List<MethodCoverage> method;

}
