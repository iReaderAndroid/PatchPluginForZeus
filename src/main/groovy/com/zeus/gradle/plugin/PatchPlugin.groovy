package com.zeus.gradle.plugin

import com.android.SdkConstants
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.zeus.gradle.plugin.utils.DebugUtils
import com.zeus.gradle.plugin.utils.PatchProcessor
import com.zeus.gradle.plugin.utils.PatchSetUtils
import com.zeus.gradle.plugin.utils.PatchUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

/**
 * 热补丁插件，为所以类动态注入以下代码
 * <p>
 *     if (Boolean.FALSE.booleanValue()){System.out.println(com.android.internal.util.Predicate.class);}* </p>
 * @author adison
 */
class PatchPlugin implements Plugin<Project> {
    public static final String EXTENSION_NAME = "patchPlugin";

    @Override
    public void apply(Project project) {
        DefaultDomainObjectSet<ApplicationVariant> variants
        if (project.getPlugins().hasPlugin(AppPlugin)) {
            variants = project.android.applicationVariants;

            project.extensions.create(EXTENSION_NAME, PatchExtension);

            applyTask(project, variants);
        }
    }

    private void applyTask(Project project, variants) {

        project.afterEvaluate {
            PatchExtension patchConfig = PatchExtension.getConfig(project);
            def includePackage = patchConfig.includePackage
            def excludeClass = patchConfig.excludeClass
            def excludePackage = patchConfig.excludePackage
            def excludeJar = patchConfig.excludeJar

            if (patchConfig.enable) {

                variants.all { variant ->
                    def dexTask = project.tasks.findByName(PatchUtils.getDexTaskName(project, variant))
                    def processManifestTask = project.tasks.findByName(PatchUtils.getProcessManifestTaskName(project, variant))

                    def manifestFile = processManifestTask.outputs.files.files[0]
                    Closure prepareClosure = {
                        patchConfig.excludePackage.add("android" + File.separator + "support")
                        def applicationClassName = PatchUtils.getApplication(manifestFile);
                        if (applicationClassName != null) {
                            applicationClassName = applicationClassName.replace(".", File.separator) + SdkConstants.DOT_CLASS
                            //过滤Application类
                            patchConfig.excludeClass.add(applicationClassName)
                        }
                    }
                    DebugUtils.debug("-------------------dexTask:" + dexTask)
                    if (dexTask != null) {
                        def patchJarBeforeDex = "patchJarBeforeDex${variant.name.capitalize()}"
                        project.task(patchJarBeforeDex) << {
                            Set<File> inputFiles = PatchUtils.getDexTaskInputFiles(project, variant, dexTask)

                            inputFiles.each { inputFile ->

                                def path = inputFile.absolutePath
                                DebugUtils.debug("patchJarBefore----->" + path)
                                if (path.endsWith(SdkConstants.DOT_JAR) && !PatchSetUtils.isExcludedJar(path, excludeJar)) {
                                    PatchProcessor.processJar(inputFile, includePackage, excludePackage, excludeClass)
                                } else if (inputFile.isDirectory()) {
                                    //intermediates/classes/debug
                                    def extensions = [SdkConstants.EXT_CLASS] as String[]

                                    def inputClasses = FileUtils.listFiles(inputFile, extensions, true);
                                    DebugUtils.debug("inputFile.isDirectory()----" + inputClasses)
                                    inputClasses.each {
                                        inputClassFile ->
                                            def classPath = inputClassFile.absolutePath
                                            if (classPath.endsWith(".class") && !classPath.contains(File.separator + "R\$") && !classPath.endsWith(File.separator + "R.class") && !classPath.endsWith(File.separator + "BuildConfig.class")) {
                                                PatchProcessor.processClass(inputClassFile)
                                            }
                                    }
                                }
                            }
                        }
                        def patchJarBeforeDexTask = project.tasks[patchJarBeforeDex]
                        DebugUtils.debug("-------------------patchJarBeforeDexTask:" + patchJarBeforeDexTask)

                        patchJarBeforeDexTask.dependsOn dexTask.taskDependencies.getDependencies(dexTask)
                        dexTask.dependsOn patchJarBeforeDexTask
                        patchJarBeforeDexTask.doFirst(prepareClosure)
                    }
                }
            }
        }
    }


}
