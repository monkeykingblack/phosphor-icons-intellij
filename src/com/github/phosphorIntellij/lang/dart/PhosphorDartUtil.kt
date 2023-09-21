package com.github.phosphorIntellij.lang.dart

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import io.flutter.FlutterUtils
import io.flutter.pub.PubRoot
import io.flutter.pub.PubRoots
import io.flutter.utils.JsonUtils
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

object PhosphorDartUtil {
    fun isHasPhosphorInProject(project: Project, element: PsiElement): Boolean {
        return FlutterUtils.isInFlutterProject(project, element) && getPhosphorPackageDir(element) != null
    }

    private fun getPhosphorPackageDir(element: PsiElement): VirtualFile? {
        val path = getPathToPhosphorPackage(element.project) ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path)
    }

    private fun getPathToPhosphorPackage(project: Project): String? {
        val packages = getPackagesFromPackageConfig(PubRoots.forProject(project))
        for (pkg in packages) {
            val jsonObj = pkg.asJsonObject
            if ("@phosphor-icons/flutter" === JsonUtils.getStringMember(jsonObj, "name")) {
                val uri = JsonUtils.getStringMember(jsonObj, "rootUri") ?: continue

                try {
                    return URI(uri).path
                } catch (_: URISyntaxException) {

                }
            }
        }
        return null
    }

    private fun getPackagesFromPackageConfig(pubRoots: List<PubRoot>): JsonArray {
        val entries = JsonArray()
        val var2: Iterator<*> = pubRoots.iterator()
        while (var2.hasNext()) {
            val pubRoot = var2.next() as PubRoot
            val configFile = pubRoot.packageConfigFile
            if (configFile != null) {
                try {
                    val contents = String(configFile.contentsToByteArray(true))
                    val element = JsonParser.parseString(contents)
                    if (element != null) {
                        val json = element.asJsonObject
                        if (JsonUtils.getIntMember(json, "configVersion") >= 2) {
                            val packages = json.getAsJsonArray("packages")
                            if (packages != null && packages.size() != 0) {
                                entries.addAll(packages)
                            }
                        }
                    }
                } catch (_: IOException) {
                }
            }
        }
        return entries
    }
}