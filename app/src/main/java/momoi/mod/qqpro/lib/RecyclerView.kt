package momoi.mod.qqpro.lib

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import momoi.mod.qqpro.asGroup

fun <T : RecyclerView> T.linearLayout() = apply {
    layoutManager = LinearLayoutManager(context)
}

fun <T : RecyclerView, E> T.content(
    data: List<E>,
    factory: Context.() -> View,
    update: ViewGroup.(E) -> Unit
) = apply {
    adapter = SimpleAdapter(data, factory, update)
}

class SimpleHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

@SuppressLint("NotifyDataSetChanged")
class SimpleAdapter<E>(
    val data: List<E>,
    val factory: Context.() -> View,
    val update: ViewGroup.(E) -> Unit
) : RecyclerView.Adapter<SimpleHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SimpleHolder(factory(parent.context))

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SimpleHolder, position: Int) {
        update(holder.itemView.asGroup(), data[position])
    }

}