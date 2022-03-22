package com.shuneault.netrunnerdeckbuilder.fragments.dialog

import android.util.SparseBooleanArray
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.game.Pack

class PackListRecyclerViewAdapter(
    private val mValues: List<Pack>,
    private val mSelectedCodes: List<String>
) : RecyclerView.Adapter<PackListRecyclerViewAdapter.ViewHolder>() {
    private val itemStateArray: SparseBooleanArray = SparseBooleanArray()

    init {
        setSelectedValues()
    }

    private fun setSelectedValues() {
        for (i in mValues.indices) {
            if (mSelectedCodes.contains(mValues[i].code)) {
                itemStateArray.put(i, true)
            }
        }
    }

    fun getSelectedValues(): ArrayList<Pack>{
        val result = ArrayList<Pack>()
        for (i in mValues.indices){
            if (itemStateArray[i]){
                result.add(mValues[i])
            }
        }
        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = mValues.size
    fun clearSelectedValues() {
        itemStateArray.clear()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {
        private val mCheck: CheckBox = mView.findViewById(R.id.item_check);
        init {
            mCheck.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val itemChecked = itemStateArray.get(adapterPosition, false)
            mCheck.isChecked = !itemChecked
            itemStateArray.put(adapterPosition, !itemChecked)
        }

        override fun toString(): String {
            return super.toString() + " '" + mCheck.text + "'"
        }

        fun bind(item: Pack, position: Int){
            mCheck.text = item.name
            mCheck.isChecked = itemStateArray.get(position, false)

            with(mView) {
                tag = item
            }
        }

    }
}
