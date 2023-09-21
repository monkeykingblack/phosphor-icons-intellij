package com.github.phosphorIntellij.lang.dart

import com.github.phosphorIntellij.PhosphorIcons
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.AstBufferUtil
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartPsiImplUtil
import io.flutter.dart.DartPsiUtil.*
import javax.swing.Icon

class PhosphorDartIconLineMarkerProvider : LineMarkerProviderDescriptor() {
    companion object {
        private val LOG = Logger.getInstance(PhosphorDartIconLineMarkerProvider::class.java)

        private val KnownEls =
            mutableSetOf("PhosphorIcons", "PhosphorIconData", "PhosphorFlatIconData", "PhosphorDuotoneIconData")

        private val KnownStyles = mutableSetOf("bold", "thin", "light", "duotone", "regular", "fill")
    }

    override fun getName(): String {
        return "ICON PREVIEW"
    }

    private fun createLineMarker(element: PsiElement?, icon: Icon): LineMarkerInfo<out PsiElement?>? {
        if (element == null) return null
        assert(element.textRange != null)
        if (!PhosphorDartUtil.isHasPhosphorInProject(element.project, element)) return null
        return LineMarkerInfo(
            element, element.textRange, icon, null, null, GutterIconRenderer.Alignment.LEFT
        ) { "" }
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<out PsiElement>? {
        if (element.node.elementType != DartTokenTypes.IDENTIFIER) return null

        val name = element.text

        if (!KnownEls.contains(name)) return null

        val refExpr = topmostReferenceExpression(element) ?: return null

        assert(ApplicationManager.getApplication() != null)

        var parent = refExpr.parent ?: return null

        val parentNode = parent.node
        if (parentNode.elementType == DartTokenTypes.CALL_EXPRESSION) {
            val icon: Icon?
            val arguments = DartPsiImplUtil.getArguments(parent as DartCallExpression) ?: return null
            val codepoint = getValueOfPositionalArgument(arguments, 0) ?: return null
            icon = if (name == "PhosphorDuotoneIconData") {
                getIconFromPackage("Duotone", codepoint)
            } else {
                val style = getPositionalArgument(arguments, 1) ?: return null
                getIconFromPackage(
                    DartPsiImplUtil.getUnquotedDartStringAndItsRange(style.text).first as String,
                    codepoint,
                )
            }
            if (icon != null) {
                return createLineMarker(element, icon)
            }

        } else if (parentNode.elementType == DartTokenTypes.SIMPLE_TYPE) {
            parent = getNewExprFromType(parent) ?: return null
            val icon: Icon?
            val arguments = DartPsiImplUtil.getArguments(parent as DartNewExpression) ?: return null
            val codepoint = getValueOfPositionalArgument(arguments, 0) ?: return null
            icon = if (name == "PhosphorDuotoneIconData") {
                getIconFromPackage("Dutone", codepoint)
            } else {
                val style = getPositionalArgument(arguments, 1) ?: return null
                getIconFromPackage(
                    DartPsiImplUtil.getUnquotedDartStringAndItsRange(style.text).first as String,
                    codepoint,
                )
            }
            if (icon != null) {
                return createLineMarker(element, icon)
            }
        } else {
            val idNode = refExpr.firstChild ?: return null
            val idNodeTexts = idNode.text.split(".")
            if (idNodeTexts.size < 2) return null

            val id = idNodeTexts[0]
            if (!KnownEls.contains(id)) return null
            val style = idNodeTexts[1]
            if (!KnownStyles.contains(style)) return null

            val selectorNode = refExpr.lastChild ?: return null
            val selector = AstBufferUtil.getTextSkippingWhitespaceComments(selectorNode.node)
            val icon: Icon? = getIconFromPackage(style, selector)
            if (icon != null) {
                return createLineMarker(element, icon)
            }
        }

        return null
    }

    private fun getIconFromPackage(style: String, name: String): Icon? {
        if (!KnownStyles.contains(style.lowercase())) return null

        return try {
            val code: Int = parseLiteralNumber(name)
            PhosphorIcons.getIconForHex(style, String.format("%1$04x", code))
        } catch (ignored: NumberFormatException) {
            PhosphorIcons.getIconForName(style, name)
        }
    }
}