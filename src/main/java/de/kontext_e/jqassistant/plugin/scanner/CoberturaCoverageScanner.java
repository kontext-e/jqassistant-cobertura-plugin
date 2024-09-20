package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.caches.ClassCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.MethodCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.PackageCache;
import de.kontext_e.jqassistant.plugin.scanner.model.*;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.util.Optional;

import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseClassName;
import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseMethodName;

public class CoberturaCoverageScanner {


    public static final String LAMBDA_METHOD_REGEX = "<.+>.+__";

    private final Store store;
    private final ClassCache classCache;
    private final MethodCache methodCache;
    private final PackageCache packageCache;

    public CoberturaCoverageScanner(Store store) {
        this.store = store;
        this.classCache = new ClassCache(store);
        this.methodCache = new MethodCache(store);
        this.packageCache = new PackageCache(store);
    }

    public static CoverageReport readCoverageReport(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(CoverageReport.class);
            return (CoverageReport) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            CoberturaCoveragePlugin.LOGGER.warn("Error reading coverage file", e);
            return null;
        }
    }

    public void saveCoverageToNeo4J(CoverageReport coverageReport, CoverageFileDescriptor coverageFileDescriptor) {
        for (PackageCoverage packageCoverage : coverageReport.getPackages()) {
            coverageFileDescriptor.getPackages().add(analyzePackage(packageCoverage));
        }
    }

    private PackageCoverageDescriptor analyzePackage(PackageCoverage packageCoverage) {
        PackageCoverageDescriptor descriptor = packageCache.findOrCreate(packageCoverage.getName());

        //TODO only do when package descriptor was newly created
        descriptor.setName(packageCoverage.getName());
        descriptor.setComplexity(packageCoverage.getComplexity());

        //TODO BranchRate
        descriptor.setBranchRate(packageCoverage.getBranchRate());

        for (ClassCoverage classCoverage : packageCoverage.getClasses()) {
            //Exclude compiler-generated classes
            if (classCoverage.getName().contains("$")) continue;

            ClassCoverageDescriptor classDescriptor = analyzeClass(classCoverage);
            if (descriptor.getClasses().contains(classDescriptor)) continue;
            descriptor.getClasses().add(classDescriptor);
        }

        //Must be done after classes have been analyzed
        updateLineCoverage(descriptor);

        return descriptor;
    }

    private static void updateLineCoverage(PackageCoverageDescriptor descriptor) {
        long coveredLines = 0;
        long totalLines = 0;
        for (ClassCoverageDescriptor classCoverage : descriptor.getClasses()) {
            for (MethodCoverageDescriptor method : classCoverage.getMethods()) {
                coveredLines += method.getLines().stream().filter(line -> line.getHits() > 0).count();
                totalLines += method.getLines().size();
            }
        }

        descriptor.setLineRate((float) coveredLines / totalLines);
    }

    private ClassCoverageDescriptor analyzeClass(ClassCoverage classCoverage) {
        String classFQN = parseClassName(classCoverage.getName());
        int beginIndex = classFQN.lastIndexOf('.');

        ClassCoverageDescriptor descriptor = classCache.findOrCreate(classFQN, classCoverage.getFileName());

        //TODO Only truly necessary when new node was created
        descriptor.setFqn(classFQN);
        descriptor.setName(beginIndex > 0? classFQN.substring(beginIndex + 1) : classFQN);
        descriptor.setComplexity(classCoverage.getComplexity());
        descriptor.setFileName(classCoverage.getFileName());

        //TODO Branch Coverage
        //descriptor.setBranchRate(classCoverage.getBranchRate());

        //Always analyse Method, in case new lines are covered
        for (MethodCoverage methodCoverage : classCoverage.getMethods()) {
            String fullMethodName = parseMethodName(methodCoverage, classCoverage);
            Matcher matcher = Pattern.compile(LAMBDA_METHOD_REGEX).matcher(fullMethodName);
            if (fullMethodName.contains("__") && matcher.find()) continue;

            MethodCoverageDescriptor methodDescriptor = analyzeMethod(methodCoverage, classCoverage);
            if (descriptor.getMethods().contains(methodDescriptor)) continue;
            descriptor.getMethods().add(methodDescriptor);
        }

        //Must be done after methods have been analyzed
        descriptor.setFirstLine(classCoverage.getFirstLine());
        descriptor.setLastLine(classCoverage.getLastLine());
        updateLineCoverage(descriptor);

        return descriptor;
    }

    private void updateLineCoverage(ClassCoverageDescriptor descriptor) {
        long coveredLines = 0;
        long totalLines = 0;
        for (MethodCoverageDescriptor method : descriptor.getMethods()) {
            coveredLines += method.getLines().stream().filter(line -> line.getHits() > 0).count();
            totalLines += method.getLines().size();
        }

        descriptor.setLineRate((float) coveredLines / totalLines);
    }

    private MethodCoverageDescriptor analyzeMethod(MethodCoverage methodCoverage, ClassCoverage classCoverage) {
        String className = parseClassName(classCoverage.getName());
        String methodName = parseMethodName(methodCoverage, classCoverage);
        String fqn = className + "." + methodName;

        MethodCoverageDescriptor descriptor = methodCache.find(fqn);
        if (descriptor == null) {
            descriptor = createNewDescriptor(methodCoverage, methodName, fqn);
        } else {
            addCoverageInformationToExistingDescriptor(descriptor, methodCoverage);
        }

        return descriptor;
    }

    private MethodCoverageDescriptor createNewDescriptor(MethodCoverage methodCoverage, String methodName, String fqn) {
        MethodCoverageDescriptor descriptor;
        descriptor = methodCache.create();
        descriptor.setName(methodName);
        descriptor.setFqn(fqn);
        descriptor.setBranchRate(methodCoverage.getBranchRate());
        descriptor.setComplexity(methodCoverage.getComplexity());
        descriptor.setSignature(methodCoverage.getSignature());
        descriptor.setFirstLine(methodCoverage.getFirstLine());
        descriptor.setLastLine(methodCoverage.getLastLine());

        for (LineCoverage lineCoverage : methodCoverage.getLines()) {
            LineCoverageDescriptor lineCoverageDescriptor = analyzeLine(lineCoverage);
            descriptor.getLines().add(lineCoverageDescriptor);
        }

        //Must be done after Lines have been analyzed
        long coveredLines = descriptor.getLines().stream().filter(line -> line.getHits() > 0).count();
        long totalLines = descriptor.getLines().size();
        descriptor.setLineRate((float) coveredLines / totalLines);

        return descriptor;
    }

    private void addCoverageInformationToExistingDescriptor(MethodCoverageDescriptor descriptor, MethodCoverage methodCoverage) {
        descriptor.getLines().forEach(lineDescriptor -> {
            Optional<LineCoverage> existingLineCoverage = methodCoverage.getLines()
                    .stream()
                    .filter(lineCoverage -> lineCoverage.getNumber() == lineDescriptor.getNumber())
                    .findFirst();

            existingLineCoverage.ifPresent(lineCoverage -> lineDescriptor.setHits(lineDescriptor.getHits() + lineCoverage.getHits()));
        });
    }
    private LineCoverageDescriptor analyzeLine(LineCoverage lineCoverage) {
        LineCoverageDescriptor descriptor = store.create(LineCoverageDescriptor.class);
        descriptor.setNumber(lineCoverage.getNumber());
        descriptor.setHits(lineCoverage.getHits());

        return descriptor;
    }
}