package de.kontext_e.jqassistant.plugin.scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameParser {

    private static final String ASYNC_METHOD_REGEX = "(?<ClassName>.+)(/|\\.)<(?<CompilerGeneratedName>.+)>.+__.+MoveNext$";
    private static final String LOCAL_METHOD_REGEX = ".*(?<ParentMethodName><.+>).*__(?<NestedMethodName>[^\\|]+)\\|.*";


    // Based on the work done by @danielpalme in https://github.com/danielpalme/ReportGenerator
    static String parseClassName(String className) {
        if (className == null) return "";

        int nestedClassSeparatorIndex = className.indexOf("/");
        if (nestedClassSeparatorIndex > -1) return className.substring(0, nestedClassSeparatorIndex);

        int GenericClassMarker = className.indexOf("`");
        if (GenericClassMarker > -1) return className.substring(0, GenericClassMarker);

        return className;
    }

    // Based on the work done by @danielpalme in https://github.com/danielpalme/ReportGenerator
    static String parseMethodName(String methodName, String className) {
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
