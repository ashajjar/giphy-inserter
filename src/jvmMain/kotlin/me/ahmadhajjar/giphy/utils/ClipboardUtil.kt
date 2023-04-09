import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

object ClipboardUtil {
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    fun copyToClipboard(img: BufferedImage?) {
        clipboard.setContents(TransferableImage(img), null)
    }

    data class TransferableImage(val i: Image?) : Transferable {
        @Throws(UnsupportedFlavorException::class)
        override fun getTransferData(flavor: DataFlavor): Any {
            return if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
                i
            } else {
                throw UnsupportedFlavorException(flavor)
            }
        }

        override fun getTransferDataFlavors(): Array<DataFlavor> {
            return arrayOf(DataFlavor.imageFlavor)
        }

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
            val flavors = transferDataFlavors
            for (dataFlavor in flavors) {
                if (flavor.equals(dataFlavor)) {
                    return true
                }
            }
            return false
        }
    }
}
