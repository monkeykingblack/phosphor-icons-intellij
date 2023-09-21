package com.github.phosphorIntellij

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.IconLoader
import java.io.IOException
import java.util.*
import javax.swing.Icon


class PhosphorIcons {
    companion object {
        private val LOG: Logger = Logger.getInstance(PhosphorIcons::class.java)

        private val icons: MutableMap<String, Properties> = mutableMapOf();

        val KNOWN_STYLES = mutableSetOf("regular", "bold", "fill", "light", "thin", "duotone")

        init {

            for (style in KNOWN_STYLES) {
                val properties = Properties()
                try {
                    properties.load(this::class.java.getResourceAsStream("/icons/$style.properties"))
                    icons[style] = properties
                } catch (e: IOException) {
                    LOG.warn(e)
                }
            }
        }

        fun getIconForHex(style: String, hexValue: String): Icon? {
            val iconName = icons[style.lowercase()]?.getProperty("$hexValue.codepoint") ?: return null
            return getIcon(style, iconName)
        }

        fun getIconForName(style: String, name: String?): Icon? {
            return getIcon(style, name);
        }

        private fun getIcon(style: String, name: String?): Icon? {
            if (name == null) {
                return null
            }
            val path = icons[style.lowercase()]?.getProperty(name) ?: return null;
            return IconLoader.findIcon("/icons/$path", PhosphorIcons::class.java)
        }
    }
}