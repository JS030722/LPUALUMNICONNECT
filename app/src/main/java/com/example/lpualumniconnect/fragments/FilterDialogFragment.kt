package com.example.lpualumniconnect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lpualumniconnect.R
import com.example.lpualumniconnect.adapter.FilterAdapter
import com.example.lpualumniconnect.datamodal.FilterType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterDialogFragment(private val filterType: FilterType, private val filterItems: List<String>, private val onApplyFilters: (Set<String>) -> Unit) : BottomSheetDialogFragment() {
    private val selectedItems = mutableSetOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_filter_dialog, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = FilterAdapter(filterItems, selectedItems)

        val applyButton: Button = view.findViewById(R.id.btn_apply_filters)
        applyButton.setOnClickListener {
            onApplyFilters(selectedItems)
            dismiss()
        }

        return view
    }
}