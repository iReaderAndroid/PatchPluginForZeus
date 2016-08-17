package com.zeus.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.Input

class PatchExtension {
    @Input
    boolean enable = false//开关：是否需要动态注入代码
    @Input
    HashSet<String> includePackage = [];//需要注入的包名列表

    @Input
    HashSet<String> excludePackage = [];//不需要注入的包名列表

    @Input
    HashSet<String> excludeClass = [];//不需要注入的类名列表
    @Input
    HashSet<String> excludeJar = [];//不需要注入的jar包

    public static PatchExtension getConfig(Project project) {
        PatchExtension config =
                project.getExtensions().findByType(PatchExtension.class);
        if (config == null) {
            config = new PatchExtension();
        }
        return config;
    }

}
