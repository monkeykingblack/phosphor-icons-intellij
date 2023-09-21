package com.github.phosphorIntellij.lang.javascript

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression

class PhosphorJSLineAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
//        LOG.warn("@@@@@ ${element::class.java} ${element.text}")
        if (element is JSAssignmentExpression) {
        }
        if (element !is PsiLiteralExpression) return

        val literalExpression: PsiLiteralExpression = element
        val value = (if (literalExpression.value is String) literalExpression.value else null) ?: return

//        LOG.warn("@@@@ $value")

    }

    companion object {
        private val LOG = Logger.getInstance(this::class.java)
    }
}