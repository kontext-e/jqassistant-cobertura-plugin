package de.kontext_e.jqassistant.plugin.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import de.kontext_e.jqassistant.plugin.scanner.caches.ClassCache;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.CoverageReport;
import de.kontext_e.jqassistant.plugin.scanner.model.MethodCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.PackageCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.CoverageFileDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.PackageCoverageDescriptor;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoberturaCoverageScanner {

    public static final String ASYNC_METHOD_REGEX = "(?<ClassName>.+)(/|\\.)<(?<CompilerGeneratedName>.+)>.+__.+MoveNext$";
    public static final String LOCAL_METHOD_REGEX = ".*(?<ParentMethodName><.+>).*__(?<NestedMethodName>[^\\|]+)\\|.*";
    public static final String LAMBDA_METHOD_REGEX = "<.+>.+__";

    private final Store store;
    private final ClassCache classCache;

    public CoberturaCoverageScanner(Store store) {
        this.store = store;
        this.classCache = new ClassCache(store);
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
        PackageCoverageDescriptor descriptor = store.create(PackageCoverageDescriptor.class);

        descriptor.setName(packageCoverage.getName());
        descriptor.setLineRate(packageCoverage.getLineRate());
        descriptor.setBranchRate(packageCoverage.getBranchRate());
        descriptor.setComplexity(packageCoverage.getComplexity());

        for (ClassCoverage classCoverage : packageCoverage.getClasses()) {
            //Exclude compiler-generated classes
            if (classCoverage.getName().contains("$")) continue;

            ClassCoverageDescriptor classDescriptor = analyzeClass(classCoverage);
            if (descriptor.getClasses().contains(classDescriptor)) continue;
            descriptor.getClasses().add(classDescriptor);
        }

        return descriptor;
    }

    private ClassCoverageDescriptor analyzeClass(ClassCoverage classCoverage) {
        String classFQN = parseClassName(classCoverage.getName());
        int beginIndex = classFQN.lastIndexOf('.');

        ClassCoverageDescriptor descriptor = classCache.findOrCreate(classFQN, classCoverage.getFileName());

        descriptor.setFqn(classFQN);
        descriptor.setName(beginIndex > 0? classFQN.substring(beginIndex + 1) : classFQN);
        descriptor.setLineRate(classCoverage.getLineRate());
        descriptor.setBranchRate(classCoverage.getBranchRate());
        descriptor.setComplexity(classCoverage.getComplexity());
        descriptor.setFileName(classCoverage.getFileName());

        for (MethodCoverage methodCoverage : classCoverage.getMethods()) {
            String fullMethodName = methodCoverage.getName() + methodCoverage.getSignature();
            Matcher matcher = Pattern.compile(LAMBDA_METHOD_REGEX).matcher(fullMethodName);
            if (matcher.find()) continue;

            MethodCoverageDescriptor methodDescriptor = analyzeMethod(methodCoverage, classCoverage);
            descriptor.getMethods().add(methodDescriptor);
        }

        //Must be done after methods have been analyzed
        descriptor.setFirstLine(classCoverage.getFirstLine());
        descriptor.setLastLine(classCoverage.getLastLine());

        return descriptor;
    }

    // Based on the work done by @danielpalme in https://github.com/danielpalme/ReportGenerator
    private static String parseClassName(String className) {
        if (className == null) return "";

        int nestedClassSeparatorIndex = className.indexOf("/");
        if (nestedClassSeparatorIndex > -1) return className.substring(0, nestedClassSeparatorIndex);

        int GenericClassMarker = className.indexOf("`");
        if (GenericClassMarker > -1) return className.substring(0, GenericClassMarker);

        return className;
    }

    private MethodCoverageDescriptor analyzeMethod(MethodCoverage methodCoverage, ClassCoverage classCoverage) {
        MethodCoverageDescriptor descriptor = store.create(MethodCoverageDescriptor.class);

        String className = parseClassName(classCoverage.getName());
        String methodName = parseMethodName(methodCoverage, classCoverage);
        descriptor.setName(methodName);
        descriptor.setFqn(className + "." + methodName);
        descriptor.setLineRate(methodCoverage.getLineRate());
        descriptor.setBranchRate(methodCoverage.getBranchRate());
        descriptor.setComplexity(methodCoverage.getComplexity());
        descriptor.setSignature(methodCoverage.getSignature());
        descriptor.setFirstLine(methodCoverage.getFirstLine());
        descriptor.setLastLine(methodCoverage.getLastLine());

        return descriptor;
    }

    // Based on the work done by @danielpalme in https://github.com/danielpalme/ReportGenerator
    private static String parseMethodName(MethodCoverage methodCoverage, ClassCoverage classCoverage) {
        String methodName = methodCoverage.getName();
        String className = classCoverage.getName();
        String fqnOfMethod = className + methodName;

        Matcher localMethodMatcher = Pattern.compile(LOCAL_METHOD_REGEX).matcher(fqnOfMethod);
        if (fqnOfMethod.contains("|") && localMethodMatcher.find()) {
            return localMethodMatcher.group("NestedMethodName");
        }

        Matcher asyncMethodMatcher = Pattern.compile(ASYNC_METHOD_REGEX).matcher(fqnOfMethod);
        if (methodName.contains("MoveNext") && asyncMethodMatcher.find()){
            return asyncMethodMatcher.group("CompilerGeneratedName");
        }

        return methodName;
    }
}