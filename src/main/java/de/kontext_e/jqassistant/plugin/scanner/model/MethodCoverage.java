package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class MethodCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private String lineRate;

    @XmlAttribute(name = "branch-rate")
    private String branchRate;

    @XmlAttribute(name = "complexity")
    private String complexity;

    @XmlAttribute(name = "signature")
    private String signature;

}
