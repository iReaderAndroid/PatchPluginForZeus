package com.zeus.gradle.plugin.utils

class PatchSetUtils {
    public static boolean isExcluded(String path, Set<String> excludePackage, Set<String> excludeClass) {
        for (String exclude : excludeClass) {
            if (path.equals(exclude)) {
                return true;
            }
        }
        for (String exclude : excludePackage) {
            if (path.startsWith(exclude)) {
                return true;
            }
        }

        return false;
    }
    public static boolean isExcludedJar(String path, Set<String> excludeJar) {
        for (String exclude : excludeJar) {
            if (path.endsWith(exclude)) {
                return true;
            }
        }

        return false;
    }
    public static boolean isIncluded(String path, Set<String> includePackage) {
        if (includePackage.size() == 0) {
            return true
        }

        def isIncluded = false;
        includePackage.each { include ->
            if (path.contains(include)) {
                isIncluded = true
            }
        }
        return isIncluded
    }
}
