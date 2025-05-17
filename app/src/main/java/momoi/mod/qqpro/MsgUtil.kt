package momoi.mod.qqpro

import android.view.View
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import com.tencent.qqnt.msg.api.impl.MsgServiceImpl
import com.tencent.qqnt.msg.api.impl.MsgUtilApiImpl
import com.tencent.watch.aio_impl.ext.MsgListUtilKt
import momoi.mod.qqpro.enum.ElementType
import momoi.mod.qqpro.enum.NTMsgType
import momoi.mod.qqpro.hook.view.MyDialogFragment
import momoi.mod.qqpro.util.Json
import momoi.mod.qqpro.util.Utils
import java.util.UUID
import kotlin.random.Random

object MsgUtil {
    val msgService = MsgServiceImpl()
    val msgUtilApi = MsgUtilApiImpl()

    fun summary(elements: List<MsgElement>): CharSequence {
        if (elements[0].elementType == ElementType.GREY_TIP) {
            //subElementType == 1
            return "[原消息已被撤回]"
        }
        elements.forEach { ele ->
            ele.picElement?.let {
                ele.picElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = "[图片]"
                }
            }
            ele.multiForwardMsgElement?.let {
                ele.multiForwardMsgElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = "[聊天记录]"
                }
            }
            ele.videoElement?.let {
                ele.videoElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = "[视频]"
                }
            }
            ele.fileElement?.let {
                ele.fileElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = "[文件]"
                }
            }
            ele.pttElement?.let {
                ele.pttElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = "[语音]"
                }
            }
            ele.arkElement?.let {
                ele.arkElement = null
                ele.elementType = ElementType.TEXT
                ele.textElement = TextElement().apply {
                    content = Json(it.bytesData).str("prompt") ?: "[卡片信息]"
                }
            }
        }
        return MsgListUtilKt.a(elements)
    }

    fun summary(record: MsgRecord) = summary(record.elements)
}

fun View.showDialog(dialog: MyDialogFragment) {
    dialog.show(
        WatchPicElementExtKt.W(this).childFragmentManager,
        Random.nextInt().toString()
    )
}