package de.kontext_e.jqassistant.plugin;

import de.kontext_e.jqassistant.plugin.model.Coverage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {

    static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        File file = new File("src/main/resources/coverage.cobertura_huge.xml");

        Coverage coverage = null;
        try {
            coverage = readCoverageFile(file);
        } catch (JAXBException e) {
            LOGGER.warn("Error reading coverage file", e);
        }
    }

    private static Coverage readCoverageFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Coverage.class);
        return (Coverage) context.createUnmarshaller().unmarshal(file);
    }
}
