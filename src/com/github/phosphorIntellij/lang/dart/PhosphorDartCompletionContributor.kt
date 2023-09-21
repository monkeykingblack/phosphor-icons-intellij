package com.github.phosphorIntellij.lang.dart

import com.github.phosphorIntellij.PhosphorIcons
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.EmptyIcon;
import com.jetbrains.lang.dart.ide.completion.DartCompletionExtension;
import com.jetbrains.lang.dart.ide.completion.DartServerCompletionContributor;
import org.apache.commons.lang.StringUtils;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.Element;

import javax.swing.*;
import java.util.Objects;

class PhosphorDartCompletionContributor : DartCompletionExtension() {
    companion object {
        private const val ICON_SIZE = 16
        private val EMPTY_ICON: Icon = EmptyIcon.create(ICON_SIZE)
    }

    override fun createLookupElement(
        project: Project,
        suggestion: CompletionSuggestion
    ): LookupElementBuilder? {
        val icon = findIcon(suggestion)
        if (icon != null) {
            val lookup =
                DartServerCompletionContributor.createLookupElement(project, suggestion).withTypeText("", icon, false)
            // Specify right alignment for type icons.
            return lookup.withTypeIconRightAligned(true)
        }
        return null
    }

    private fun findIcon(suggestion: CompletionSuggestion): Icon? {
        val element: Element? = suggestion.element
        if (element != null) {
            val returnType: String? = element.returnType
            if (!StringUtils.isEmpty(returnType)) {
                val name: String = element.name
                val declaringType = suggestion.declaringType
                if (Objects.equals(declaringType, "PhosphorIconsBold")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("bold", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                } else if (Objects.equals(declaringType, "PhosphorIconsDuotone")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("duotone", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                } else if (Objects.equals(declaringType, "PhosphorIconsFill")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("Fill", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                } else if (Objects.equals(declaringType, "PhosphorIconsLight")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("light", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                } else if (Objects.equals(declaringType, "PhosphorIconsRegular")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("regular", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                } else if (Objects.equals(declaringType, "PhosphorIconsThin")) {
                    val icon: Icon? = PhosphorIcons.getIconForName("thin", name)
                    // If we have no icon, show an empty node (which is preferable to the default "IconData" text).
                    return icon ?: EMPTY_ICON
                }
            }
        }
        return null
    }
}