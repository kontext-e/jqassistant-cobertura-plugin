package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
@NoArgsConstructor
public class MethodCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private float lineRate;

    @XmlAttribute(name = "branch-rate")
    private float branchRate;

    @XmlAttribute(name = "complexity")
    private int complexity;

    @XmlAttribute(name = "signature")
    private String signature;

    @XmlElement(name = "line")
    @XmlElementWrapper(name = "lines")
    private List<LineCoverage> lines;

    private int firstLine;
    private int lastLine;

    public int getFirstLine() {
        return lines.stream().map(LineCoverage::getNumber).min(Integer::compareTo).orElse(0);
    }

    public int getLastLine() {
        return lines.stream().map(LineCoverage::getNumber).max(Integer::compareTo).orElse(0);
    }

}
