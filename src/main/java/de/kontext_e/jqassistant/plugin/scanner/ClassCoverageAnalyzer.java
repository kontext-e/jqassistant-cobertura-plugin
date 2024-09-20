package de.kontext_e.jqassistant.plugin.scanner;

import de.kontext_e.jqassistant.plugin.scanner.caches.ClassCache;
import de.kontext_e.jqassistant.plugin.scanner.model.ClassCoverage;
import de.kontext_e.jqassistant.plugin.scanner.model.MethodCoverage;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.ClassCoverageDescriptor;
import de.kontext_e.jqassistant.plugin.scanner.store.descriptor.MethodCoverageDescriptor;

import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseClassName;
import static de.kontext_e.jqassistant.plugin.scanner.NameParser.parseMethodName;

public class ClassCoverageAnalyzer {

    public static final String LAMBDA_METHOD_REGEX = "<.+>.+__";

    private final ClassCache classCache;
    private final MethodCoverageAnalyzer methodCoverageAnalyzer;

    public ClassCoverageAnalyzer(ClassCache classCache, MethodCoverageAnalyzer methodCoverageAnalyzer) {
        this.classCache = classCache;
        this.methodCoverageAnalyzer = methodCoverageAnalyzer;
    }

    ClassCoverageDescriptor analyzeClass(ClassCoverage classCoverage) {
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

            MethodCoverageDescriptor methodDescriptor = methodCoverageAnalyzer.analyzeMethod(methodCoverage, classCoverage);
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

}
