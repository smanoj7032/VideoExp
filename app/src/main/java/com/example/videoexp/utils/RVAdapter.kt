package com.example.videoexp.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class RVAdapter<M, B : ViewDataBinding>(private var layId: Int, private val modelVarId: Int) :
    RecyclerView.Adapter<RVAdapter.Holder<B>>() {
    private val dataList: MutableList<M> = ArrayList()

    class Holder<S : ViewDataBinding>(var binding: S) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<B> {
        val binding: B =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layId, parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: Holder<B>, position: Int) {
        onBind(holder.binding, dataList[position], position)
        holder.binding.executePendingBindings()
    }

    open fun onBind(binding: B, bean: M, position: Int) {
        binding.setVariable(modelVarId, bean)
    }

    var list: List<M>?
        get() = dataList
        set(newList) {
            dataList.clear()
            if (newList != null) dataList.addAll(newList)
            notifyDataSetChanged()
        }
}