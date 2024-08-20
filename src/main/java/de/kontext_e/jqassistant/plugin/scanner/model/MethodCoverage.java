package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.*;

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

}
