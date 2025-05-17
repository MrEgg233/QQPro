package momoi.mod.qqpro.hook.aio_cell

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.tencent.watch.aio_impl.data.WatchAIOMsgItem
import com.tencent.watch.aio_impl.ui.cell.base.BaseWatchItemCell
import com.tencent.watch.aio_impl.ui.widget.AIOCellGroupWidget
import momoi.anno.mixin.Mixin
import momoi.mod.qqpro.enum.NTMsgType
import momoi.mod.qqpro.hook.action.CurrentContact
import momoi.mod.qqpro.lib.create
import momoi.mod.qqpro.warp
import java.lang.ref.WeakReference
import java.util.WeakHashMap

object AIOCell {
    val hooks = mutableListOf<Hook<*>>()
    init {
        addHook<ReplyView>(
            type = NTMsgType.REPLY,
            onBind = { msg, widget ->
                val reply = msg.elements.firstNotNullOf { it.replyElement }
                loadData(CurrentContact, reply)
                setOnClickListener(ReplyClick(widget, reply))
            },
            appendMode = true
        )
        addHook<ForwardMsgView>(
            type = NTMsgType.MULTIMSGFORWARD,
            onBind = { msg, widget ->
                if (msg.forwardData == null) {
                    msg.forwardData = ForwardMsgData(CurrentContact, msg, msg)
                }
                loadData(CurrentContact, msg.forwardData!!)
            },
        )
        addHook<CardMsgView>(
            type = NTMsgType.ARKSTRUCT,
            onBind = { msg, widget ->
                loadData(msg.elements.firstNotNullOf { it.arkElement })
            }
        )
    }
    inline fun <reified T : View> addHook(
        type: Int,
        noinline onBind: T.(MsgRecordEx, AIOCellGroupWidget) -> Unit,
        appendMode: Boolean = false
    ) {
        hooks.add(
            Hook(
                type = type,
                onBind = onBind,
                createView = { create<T>(it) },
                appendMode = appendMode
            )
        )
    }

    class Hook<T : View>(
        val type: Int,
        private val onBind: T.(MsgRecordEx, AIOCellGroupWidget) -> Unit,
        val createView: (Context) -> T,
        val appendMode: Boolean
    ) {
        private val views = WeakHashMap<AIOCellGroupWidget, WeakReference<T>>()
        fun bind(widget: AIOCellGroupWidget, view: View, msg: MsgRecordEx) {
            view.visibility = View.VISIBLE
            if (!appendMode) {
                widget.contentWidget.visibility = View.GONE
            }
            onBind(view as T, msg, widget)
        }
        fun getOrCreate(widget: AIOCellGroupWidget): T {
            return views.getOrPut(widget) {
                val view = createView(widget.context)
                val warp = widget.contentWidget.warp()
                warp.addView(view, 0)
                WeakReference(view)
            }.get()!!
        }
        fun recover(widget: AIOCellGroupWidget) {
            views.get(widget)?.get()?.let {
                it.visibility = View.GONE
                if (!appendMode) {
                    widget.contentWidget.visibility = View.VISIBLE
                }
            }
        }
    }

    @Mixin
    class HookCell : BaseWatchItemCell() {
        override fun reply_item(view: View?, watchAIOMsgItem: WatchAIOMsgItem) {}
        override fun i(
            view: View?,
            item: WatchAIOMsgItem,
            p3: Int,
            p4: MutableList<*>?,
            p5: Lifecycle?,
            p6: LifecycleOwner?
        ) {
            super.i(view, item, p3, p4, p5, p6)
            val widget = view as? AIOCellGroupWidget ?: return
            hooks.forEach {
                if (item.d.msgType == it.type) {
                    val view = it.getOrCreate(widget)
                    it.bind(widget, view, item.d as MsgRecordEx)
                } else {
                    it.recover(widget)
                }
            }
            (widget.contentWidget as? TextView)?.layoutParams?.let {
                it.width = ViewGroup.LayoutParams.WRAP_CONTENT
                it.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }
}