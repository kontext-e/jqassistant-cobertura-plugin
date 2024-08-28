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
public class LineCoverage {

    @XmlAttribute(name = "number")
    private int number;

    @XmlAttribute(name = "hits")
    private int hits;

}
