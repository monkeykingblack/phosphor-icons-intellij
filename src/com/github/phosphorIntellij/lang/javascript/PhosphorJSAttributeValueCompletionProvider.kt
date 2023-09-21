package com.github.phosphorIntellij.lang.javascript

import com.github.phosphorIntellij.PhosphorIcons
import com.google.common.base.CaseFormat
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.lang.javascript.psi.ecma6.impl.JSXXmlLiteralExpressionImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.css.impl.util.completion.CssClassOrIdReferenceCompletionContributor
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext

class PhosphorJSAttributeValueCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val tag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java, false) ?: return
        val xmlAttribute = PsiTreeUtil.getParentOfType(parameters.position, XmlAttribute::class.java, false) ?: return
        if (tag is HtmlTag && ("class" == xmlAttribute.name || "className" == xmlAttribute.name)) {
            val ref = CssClassOrIdReferenceCompletionContributor.getCssClassOrIdReference(
                parameters.offset,
                parameters.position.containingFile
            )
            if (ref != null) {
                htmlCssClassCompletion(ref, parameters, result)
            } else if (tag is JSXXmlLiteralExpressionImpl) {
                jsxCssClassCompletion(parameters, result)
                result.stopHere()
            }
        }
    }

    private fun htmlCssClassCompletion(
        ref: HtmlCssClassOrIdReference,
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        var xmlAttributeValue =
            PsiTreeUtil.getParentOfType(parameters.position, XmlAttribute::class.java, false)?.value ?: ""
        xmlAttributeValue = xmlAttributeValue.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
        var style = "regular"
        for (value in xmlAttributeValue.split(" ").reversed()) {
            if (!value.startsWith("ph-")) continue
            val valueNoPrefix = value.replace("ph-", "")
            if (PhosphorIcons.KNOWN_STYLES.contains(valueNoPrefix)) {
                style = valueNoPrefix
                break
            }
        }

        ref.addCompletions(parameters, result.prefixMatcher) { element: LookupElement? ->
            if (element != null) findIcon(element, result, style)
        }
        result.stopHere()

    }

    private fun jsxCssClassCompletion(parameters: CompletionParameters, result: CompletionResultSet) {
        var xmlAttributeValue =
            PsiTreeUtil.getParentOfType(parameters.position, XmlAttribute::class.java, false)?.value ?: ""
        xmlAttributeValue = xmlAttributeValue.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
        var style = "regular"
        for (s in PhosphorIcons.KNOWN_STYLES) {
            xmlAttributeValue.contains("ph-$s")
            style = s
        }
        var prefixMatcher = result.prefixMatcher
        val prefix = prefixMatcher.prefix
        var i = prefix.length - 1
        while (i >= 0 && Character.isJavaIdentifierPart(prefix[i])) {
            --i
        }
        if (i != -1) {
            prefixMatcher = prefixMatcher.cloneWithPrefix(prefix.substring(i + 1))
        }
        val sorter = JSCompletionContributor.createOwnSorter(parameters)
        val completionResultSet = result.withRelevanceSorter(sorter)

        completionResultSet.runRemainingContributors(parameters) { completionResult ->
            findIcon(completionResult.lookupElement, completionResultSet, style)
        }
        result.stopHere()
    }

    private fun findIcon(element: LookupElement, result: CompletionResultSet, style: String) {
        var builder = if (element is PrioritizedLookupElement<*>) {
            element.delegate as LookupElementBuilder
        } else {
            element as LookupElementBuilder
        }

        val lookupString = builder.lookupString
        if (lookupString.startsWith("ph-")) {
            val iconName = lookupString.replace("ph-", "")
            val icon = PhosphorIcons.getIconForName(
                style,
                CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, iconName)
            )
            if (icon != null) {
                builder = builder.withIcon(icon)
            }
        }
        result.withPrefixMatcher(result.prefixMatcher).addElement(builder)
    }

    companion object {
        private val LOG = Logger.getInstance(this::class.java)
    }
}