package momoi.mod.qqpro.hook.view

import android.content.Context
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.ReplyElement
import momoi.anno.mixin.Mixin
import momoi.mod.qqpro.Colors
import momoi.mod.qqpro.MsgUtil
import momoi.mod.qqpro.Utils
import momoi.mod.qqpro.join
import momoi.mod.qqpro.lib.FILL
import momoi.mod.qqpro.lib.WRAP
import momoi.mod.qqpro.lib.background
import momoi.mod.qqpro.lib.content
import momoi.mod.qqpro.lib.dp
import momoi.mod.qqpro.lib.marginHorizontal
import momoi.mod.qqpro.lib.paddingHorizontal
import momoi.mod.qqpro.lib.textColor
import momoi.mod.qqpro.lib.textSize
import momoi.mod.qqpro.lib.vertical

@Mixin
class ReplyElementEx : ReplyElement() {
    var senderName: String? = null
    var content: CharSequence? = null
}

class ReplyView(context: Context) : LinearLayout(context) {
    private lateinit var mTvName: TextView
    private lateinit var mTvContent: TextView
    private var currentMsgSeq: Long = 0

    init {
        this.vertical()
            .marginHorizontal(2.dp)
            .background(Colors.replyBackground)
            .paddingHorizontal(2.dp)
            .content {
                mTvName = add<TextView>()
                    .textSize(10f)
                    .textColor(Colors.replyText)
                mTvContent = add<TextView>()
                    .textSize(12f)
                    .textColor(Colors.replyText)
                    .apply {
                        maxLines = 2
                        ellipsize = TextUtils.TruncateAt.END
                    }
            }
    }

    fun loadData(contact: Contact, replyElement: ReplyElement) {
        val reply = replyElement as ReplyElementEx
        currentMsgSeq = reply.replayMsgSeq
        if (reply.senderName == null) {
            val sendTime = Utils.formatTime(reply.replyMsgTime * 1000)
            reply.content = reply.sourceMsgTextElems.join { it.textElemContent ?: " " }
            reply.senderName = "${reply.senderUid} $sendTime"
            MsgUtil.msgService.getSingleMsg(contact, reply.replayMsgSeq) { _, _, msgRecords ->
                msgRecords?.getOrNull(0)?.let {
                    reply.senderName = buildString {
                        append(it.sendMemberName.ifEmpty { it.sendNickName })
                        append(" ")
                        append(sendTime)
                    }
                    reply.content = MsgUtil.summary(it)
                    if (currentMsgSeq == reply.replayMsgSeq) {
                        post {
                            loadData(contact, reply)
                        }
                    }
                }
            }
        }
        mTvName.text = reply.senderName
        mTvContent.text = reply.content
    }
}