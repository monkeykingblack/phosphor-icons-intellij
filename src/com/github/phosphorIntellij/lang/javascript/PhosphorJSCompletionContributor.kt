package com.github.phosphorIntellij.lang.javascript

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns

class PhosphorJSCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttribute()),
            PhosphorJSAttributeValueCompletionProvider()
        )
    }

    companion object {
        private val LOG = Logger.getInstance(this::class.java)
    }
}