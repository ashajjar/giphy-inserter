package me.ahmadhajjar.giphy.utils

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.*

class BasicTransferable(
    private var plainData: String,
    private var hTMLData: String
) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return (richerFlavors ?: arrayOf()) + htmlFlavors + plainFlavors + stringFlavors
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        val flavors = transferDataFlavors
        for (i in flavors.indices) {
            if (flavors[i].equals(flavor)) {
                return true
            }
        }
        return false
    }

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (isRicherFlavor(flavor)) {
            return getRicherData(flavor)!!
        } else if (isHTMLFlavor(flavor)) {
            val data = hTMLData
            if (String::class.java == flavor.representationClass) {
                return data
            } else if (Reader::class.java == flavor.representationClass) {
                return StringReader(data)
            } else if (InputStream::class.java == flavor.representationClass) {
                return createInputStream(data)
            }
            // fall through to unsupported
        } else if (isPlainFlavor(flavor)) {
            val data = plainData
            if (String::class.java == flavor.representationClass) {
                return data
            } else if (Reader::class.java == flavor.representationClass) {
                return StringReader(data)
            } else if (InputStream::class.java == flavor.representationClass) {
                return createInputStream(data)
            }
            // fall through to unsupported
        } else if (isStringFlavor(flavor)) {
            return plainData
        }
        throw UnsupportedFlavorException(flavor)
    }

    @Throws(IOException::class, UnsupportedFlavorException::class)
    private fun createInputStream(data: String): InputStream {
        return ByteArrayInputStream(data.toByteArray(charset("UTF-8")))
    }

    protected fun isRicherFlavor(flavor: DataFlavor?): Boolean {
        val richerFlavors = richerFlavors
        val nFlavors = richerFlavors?.size ?: 0
        for (i in 0 until nFlavors) {
            if (richerFlavors!![i].equals(flavor)) {
                return true
            }
        }
        return false
    }

    private val richerFlavors: Array<DataFlavor>?
        get() = null

    @Throws(UnsupportedFlavorException::class)
    private fun getRicherData(flavor: DataFlavor?): Any? {
        return null
    }

    protected fun isHTMLFlavor(flavor: DataFlavor?): Boolean {
        val flavors: Array<DataFlavor> = htmlFlavors
        for (i in flavors.indices) {
            if (flavors[i].equals(flavor)) {
                return true
            }
        }
        return false
    }

    private val isHTMLSupported: Boolean
        get() = hTMLData != null

    private fun isPlainFlavor(flavor: DataFlavor?): Boolean {
        val flavors: Array<DataFlavor> = plainFlavors
        for (i in flavors.indices) {
            if (flavors[i].equals(flavor)) {
                return true
            }
        }
        return false
    }

    private val isPlainSupported: Boolean
        get() = plainData != null

    private fun isStringFlavor(flavor: DataFlavor?): Boolean {
        val flavors: Array<DataFlavor> = stringFlavors
        for (i in flavors.indices) {
            if (flavors[i].equals(flavor)) {
                return true
            }
        }
        return false
    }

    companion object {
        private lateinit var htmlFlavors: Array<DataFlavor>
        private lateinit var stringFlavors: Array<DataFlavor>
        private lateinit var plainFlavors: Array<DataFlavor>

        init {
            try {
                htmlFlavors = arrayOf(
                    DataFlavor("text/html;class=java.lang.String"),
                    DataFlavor("text/html;class=java.io.Reader"),
                    DataFlavor("text/html;charset=unicode;class=java.io.InputStream")
                )

                plainFlavors = arrayOf(
                    DataFlavor("text/plain;class=java.lang.String"),
                    DataFlavor("text/plain;class=java.io.Reader"),
                    DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"),
                )
                stringFlavors = arrayOf(
                    DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String"),
                    DataFlavor.stringFlavor,
                )
            } catch (cle: ClassNotFoundException) {
                println("error initializing javax.swing.plaf.basic.BasicTranserable")
            }
        }
    }
}
