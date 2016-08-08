package com.zeus.gradle.plugin.utils

import com.android.SdkConstants
import com.android.build.gradle.api.BaseVariant
import com.google.common.collect.Sets
import groovy.xml.Namespace
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.gradle.api.Task

public class PatchUtils {


    public static String getApplication(File manifestFile) {
        def manifest = new XmlParser().parse(manifestFile)
        def androidTag = new Namespace("http://schemas.android.com/apk/res/android", 'android')
        return manifest.application[0].attribute(androidTag.name)
    }

    private static List<String> getFilesHash(String baseDirectoryPath, File directoryFile) {
        List<String> javaFiles = new ArrayList<String>();

        File[] children = directoryFile.listFiles();
        if (children == null) {
            return javaFiles;
        }

        for (final File file : children) {
            if (file.isDirectory()) {
                List<String> tempList = getFilesHash(baseDirectoryPath, file);
                if (!tempList.isEmpty()) {
                    javaFiles.addAll(tempList);
                }
            } else {
                InputStream is = new FileInputStream(file);
                def hash = DigestUtils.shaHex(IOUtils.toByteArray(is))
                javaFiles.add(hash)

                is.close()
            }
        }

        return javaFiles;
    }



    static String getProcessManifestTaskName(Project project, BaseVariant variant) {
        return "process${variant.name.capitalize()}Manifest"
    }


    /**
     * 获取Dex任务名
     * @param project
     * @param variant
     * @return
     */
    static String getDexTaskName(Project project, BaseVariant variant) {
        if (isUseTransformAPI(project)) {
            return "transformClassesWithDexFor${variant.name.capitalize()}"
        } else {
            return "dex${variant.name.capitalize()}"
        }
    }


    static Set<File> getDexTaskInputFiles(Project project, BaseVariant variant, Task dexTask) {
        if (dexTask == null) {
            dexTask = project.tasks.findByName(getDexTaskName(project, variant));
        }
        DebugUtils.debug("getDexTaskInputFiles--------->" + dexTask)
        if (isUseTransformAPI(project)) {
            def extensions = [SdkConstants.EXT_JAR] as String[]
            DebugUtils.debug("getDexTaskInputFiles---isUseTransformAPI---extensions--->" + extensions)

            Set<File> files = Sets.newHashSet();

            dexTask.inputs.files.files.each {
                if (it.exists()) {
                    DebugUtils.debug("getDexTaskInputFiles---isUseTransformAPI------>" + it.absolutePath+","+"intermediates/classes/${variant.name.capitalize()}")
                    if (it.isDirectory()) {
                        Collection<File> jars = FileUtils.listFiles(it, extensions, true);
                        files.addAll(jars)

                        if (it.absolutePath.toLowerCase().endsWith(("intermediates"+File.separator+"classes"+File.separator+variant.name.capitalize()).toLowerCase())) {
                            DebugUtils.debug("getDexTaskInputFiles---isUseTransformAPI---endsWith DOT_JAR--->" + it.absolutePath)
                            files.add(it)
                        }
                    } else if (it.name.endsWith(SdkConstants.DOT_JAR)) {
                        DebugUtils.debug("getDexTaskInputFiles---isUseTransformAPI---DOT_JAR--->" + it.absolutePath)
                        files.add(it)
                    }
                }
            }
            return files
        } else {
            return dexTask.inputs.files.files;
        }
    }


    public static boolean isUseTransformAPI(Project project) {
        return compareVersionName(project.gradle.gradleVersion, "1.4.0") >= 0;
    }


    private static int compareVersionName(String str1, String str2) {
        String[] thisParts = str1.split("-")[0].split("\\.");
        String[] thatParts = str2.split("-")[0].split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

}