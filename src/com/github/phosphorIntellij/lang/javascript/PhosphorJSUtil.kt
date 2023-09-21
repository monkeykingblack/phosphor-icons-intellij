package com.github.phosphorIntellij.lang.javascript

import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object PhosphorJSUtil {
    private val KNOWN_PACKAGE =
        mutableListOf(
            "@phosphor-icons/web",
            "@phosphor-icons/react",
            "@phosphor-icons/vue",
            "@phosphor-icons/elm",
            "@phosphor-icons/webcomponents"
        )

    fun isHasPhosphorInProject(project: Project, contextFileOrDir: VirtualFile): Boolean {
        return getPhosphorPackageDir(project, contextFileOrDir) != null
    }

    private fun getPhosphorPackageDir(project: Project, contextFileOrDir: VirtualFile): VirtualFile? {
        val packageJson = PackageJsonUtil.findUpPackageJson(contextFileOrDir) ?: return null

        for (pkg in KNOWN_PACKAGE) {
            val phosphorPackage =
                NodeInstalledPackageFinder(project, packageJson).findInstalledPackage(pkg)
            return phosphorPackage?.packageDir
        }

        return null
    }
}