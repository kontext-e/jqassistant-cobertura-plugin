package de.kontext_e.jqassistant.plugin.scanner.model;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "coverage")
@XmlAccessorType(XmlAccessType.FIELD)
public class Coverage {

    @XmlElement(name = "package")
    @XmlElementWrapper(name = "packages")
    private List<Package> packages;

}
