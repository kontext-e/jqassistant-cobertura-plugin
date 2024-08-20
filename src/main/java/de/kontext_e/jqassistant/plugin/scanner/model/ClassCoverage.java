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
public class ClassCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private float lineRate;

    @XmlAttribute(name = "branch-rate")
    private float branchRate;

    @XmlAttribute(name = "complexity")
    private int complexity;

    @XmlAttribute(name = "filename")
    private String fileName;

    @XmlElement(name = "method")
    @XmlElementWrapper(name = "methods")
    private List<MethodCoverage> methods;

    private int firstLine;
    private int lastLine;

    public int getFirstLine() {
        return methods.stream().map(MethodCoverage::getFirstLine).min(Integer::compareTo).orElse(0);
    }

    public int getLastLine() {
        return methods.stream().map(MethodCoverage::getLastLine).max(Integer::compareTo).orElse(0);
    }

}
