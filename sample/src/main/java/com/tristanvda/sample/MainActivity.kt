package com.tristanvda.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tristanvda.combinewrapperlistadapter.adapter.CombineWrapperListAdapter
import com.tristanvda.sample.adapter.ColorAdapter
import com.tristanvda.sample.adapter.WordAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val combineWrapperListAdapter = CombineWrapperListAdapter()
    private val wordAdapter: WordAdapter = WordAdapter()
    private val colorAdapter: ColorAdapter = ColorAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = combineWrapperListAdapter

        combineWrapperListAdapter.add(wordAdapter)
    }
}