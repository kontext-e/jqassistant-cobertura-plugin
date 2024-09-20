package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.caches.ClassCache;
import de.kontext_e.jqassistant.plugin.scanner.caches.LineCache;
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

    private final ClassCache classCache;
    private final MethodCache methodCache;
    private final PackageCache packageCache;
    private final LineCache lineCache;

    public CoberturaCoverageScanner(Store store) {
        this.classCache = new ClassCache(store);
        this.methodCache = new MethodCache(store);
        this.packageCache = new PackageCache(store);
        this.lineCache = new LineCache(store);
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
        PackageCoverageDescriptor descriptor = packageCache
                .find(packageCoverage.getName())
                .orElseGet(() -> createPackageCoverageDescriptor(packageCoverage));

        analyseClasses(packageCoverage, descriptor);

        //Must be done after classes have been analyzed
        updateLineCoverage(descriptor);
        //TODO BranchRate descriptor.setBranchRate(packageCoverage.getBranchRate());

        return descriptor;
    }

    private PackageCoverageDescriptor createPackageCoverageDescriptor(PackageCoverage packageCoverage) {
        PackageCoverageDescriptor descriptor = packageCache.create(packageCoverage.getName());
        descriptor.setName(packageCoverage.getName());
        descriptor.setComplexity(packageCoverage.getComplexity());

        return descriptor;
    }

    private void analyseClasses(PackageCoverage packageCoverage, PackageCoverageDescriptor descriptor) {
        for (ClassCoverage classCoverage : packageCoverage.getClasses()) {
            //Exclude compiler-generated classes
            if (classCoverage.getName().contains("$")) continue;

            ClassCoverageDescriptor classDescriptor = analyzeClass(classCoverage);
            if (descriptor.getClasses().contains(classDescriptor)) continue;
            descriptor.getClasses().add(classDescriptor);
        }
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

        ClassCoverageDescriptor descriptor = classCache
                .find(classFQN, classCoverage.getFileName())
                .orElseGet(() -> createClassCoverageDescriptor(classCoverage, classFQN));

        //Always analyse Method, in case new lines are covered
        analyzeMethods(classCoverage, descriptor);

        //Must be done after methods have been analyzed
        descriptor.setFirstLine(classCoverage.getFirstLine());
        descriptor.setLastLine(classCoverage.getLastLine());
        updateLineCoverage(descriptor);
        //TODO Branch Coverage descriptor.setBranchRate(classCoverage.getBranchRate());

        return descriptor;
    }

    private void analyzeMethods(ClassCoverage classCoverage, ClassCoverageDescriptor descriptor) {
        for (MethodCoverage methodCoverage : classCoverage.getMethods()) {
            String fullMethodName = parseMethodName(methodCoverage.getName(), classCoverage.getName());
            if (fullMethodName.contains("__") && fullMethodName.matches(LAMBDA_METHOD_REGEX)) continue;

            MethodCoverageDescriptor methodDescriptor = analyzeMethod(methodCoverage, classCoverage);
            if (descriptor.getMethods().contains(methodDescriptor)) continue;
            descriptor.getMethods().add(methodDescriptor);
        }
    }

    private ClassCoverageDescriptor createClassCoverageDescriptor(ClassCoverage classCoverage, String classFQN) {
        ClassCoverageDescriptor descriptor = classCache.createDescriptor(classCoverage.getName(), classCoverage.getFileName());
        int beginIndex = classFQN.lastIndexOf('.');

        descriptor.setFqn(classFQN);
        descriptor.setName(beginIndex > 0? classFQN.substring(beginIndex + 1) : classFQN);
        descriptor.setComplexity(classCoverage.getComplexity());
        descriptor.setFileName(classCoverage.getFileName());

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
        String methodName = parseMethodName(methodCoverage.getName(), classCoverage.getName());
        String fqn = className + "." + methodName;

        MethodCoverageDescriptor methodDescriptor = methodCache.find(fqn).orElseGet(() -> createNewDescriptor(methodCoverage, methodName, fqn));

        for (LineCoverage lineCoverage : methodCoverage.getLines()) {
            LineCoverageDescriptor lineCoverageDescriptor = analyzeLine(lineCoverage, fqn);
            if (methodDescriptor.getLines().contains(lineCoverageDescriptor)) continue;
            methodDescriptor.getLines().add(lineCoverageDescriptor);
        }

        //FIXME "EMS.APIService.Persistence.Context.DataContext.OnModelCreating"
        //Must be done after Lines have been analyzed
        long coveredLines = methodDescriptor.getLines().stream().filter(line -> line.getHits() > 0).count();
        long totalLines = methodDescriptor.getLines().size();
        methodDescriptor.setLineRate((float) coveredLines / totalLines);

        return methodDescriptor;
    }

    private MethodCoverageDescriptor createNewDescriptor(MethodCoverage methodCoverage, String methodName, String fqn) {
        MethodCoverageDescriptor descriptor = methodCache.create();
        descriptor.setName(methodName);
        descriptor.setFqn(fqn);
        descriptor.setBranchRate(methodCoverage.getBranchRate());
        descriptor.setComplexity(methodCoverage.getComplexity());
        descriptor.setSignature(methodCoverage.getSignature());
        descriptor.setFirstLine(methodCoverage.getFirstLine());
        descriptor.setLastLine(methodCoverage.getLastLine());

        return descriptor;
    }

    private LineCoverageDescriptor analyzeLine(LineCoverage lineCoverage, String fqn) {
        Optional<LineCoverageDescriptor> descriptor = lineCache.find(fqn, lineCoverage.getNumber());

        LineCoverageDescriptor lineCoverageDescriptor;
        if (descriptor.isPresent()) {
            lineCoverageDescriptor = descriptor.get();
            lineCoverageDescriptor.setHits(lineCoverageDescriptor.getHits() + lineCoverage.getHits());
        } else {
            lineCoverageDescriptor = lineCache.createDescriptor(fqn, lineCoverage.getNumber());
            lineCoverageDescriptor.setNumber(lineCoverage.getNumber());
            lineCoverageDescriptor.setHits(lineCoverage.getHits());
        }
        return lineCoverageDescriptor;
    }
}