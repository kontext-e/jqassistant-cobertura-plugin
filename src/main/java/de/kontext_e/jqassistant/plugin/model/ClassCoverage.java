package de.kontext_e.jqassistant.plugin.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassCoverage {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "line-rate")
    private String lineRate;

    @XmlAttribute(name = "branch-rate")
    private String branchRate;

    @XmlAttribute(name = "complexity")
    private String complexity;


    @XmlElement(name = "method")
    @XmlElementWrapper(name = "methods")
    private List<MethodCoverage> method;

}
