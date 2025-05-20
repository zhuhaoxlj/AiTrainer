package com.lindum.mlkit.vision.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.result.DotTimeLineAdapter
import com.lindum.mlkit.vision.result.Event
import com.lindum.mlkit.vision.utils.FileUtils
import com.lindum.mlkit.vision.view.BubbleView
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration
import com.vivian.timelineitemdecoration.util.Util
import com.xuexiang.xui.utils.StatusBarUtils
import java.util.ArrayList
import java.util.Collections

class DiaryFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DotTimeLineAdapter
    private lateinit var itemDecoration: DotItemDecoration
    private val eventList = ArrayList<Event?>()
    private val times = ArrayList<Long>()
    private val events = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_train_diary, container, false)
            initView()
        }
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // 每次显示时刷新日记数据
        loadTrainingHistory()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initView() {
        rootView?.let { view ->
            // 状态栏背景条
            val statusBar = view.findViewById<View>(R.id.v_fake_status_bar)
            val params = statusBar.layoutParams
            params.height = StatusBarUtils.getStatusBarHeight(requireContext())
            statusBar.layoutParams = params

            // 初始化RecyclerView
            recyclerView = view.findViewById(R.id.recycler_view)
            recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            // 配置时间线装饰
            itemDecoration = DotItemDecoration.Builder(requireContext())
                .setOrientation(DotItemDecoration.VERTICAL)
                .setItemStyle(DotItemDecoration.STYLE_DRAW)
                .setTopDistance(20f) // dp
                .setItemInterVal(10f) // dp
                .setItemPaddingLeft(10f)
                .setItemPaddingRight(10f)
                .setDotColor(Color.parseColor("#FFFFFF"))
                .setDotRadius(4) // dp
                .setDotPaddingTop(20)
                .setDotInItemOrientationCenter(false)
                .setLineColor(Color.parseColor("#FFFFFF"))
                .setLineWidth(1f) // dp
                .setEndText("终")
                .setTextColor(Color.WHITE)
                .setTextSize(12f) // sp
                .setDotPaddingText(15f)
                .setBottomDistance(40f)
                .create()

            itemDecoration.setSpanIndexListener { view, spanIndex ->
                val bubbleView = view.findViewById<BubbleView>(R.id.bv_background)
                bubbleView.setArrow(spanIndex)
            }

            if (recyclerView.itemDecorationCount == 0) {
                recyclerView.addItemDecoration(itemDecoration)
            }

            // 初始化适配器
            adapter = DotTimeLineAdapter(requireContext(), eventList)
            recyclerView.adapter = adapter

            // 加载训练历史
            loadTrainingHistory()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTrainingHistory() {
        // 清空现有数据
        times.clear()
        events.clear()
        eventList.clear()

        // 读取训练历史记录
        val trainingHistory = FileUtils.readFileToString(
            "${MainActivity.Companion.appFilePath}/files/analysisReport/",
            "recordFile.sys"
        )

        for (record in trainingHistory) {
            val parts = record.split(" ", limit = 2)
            if (parts.size >= 2) {
                val time = parts[0].toLong()
                val event = parts[1]

                Util.LongtoStringFormat(1000 * time)
                times.add(time)
                events.add(event)
            }
        }

        // 反转顺序，最新的事件显示在前面
        Collections.reverse(times)
        Collections.reverse(events)

        // 填充事件列表
        for (i in times.indices) {
            val event = Event()
            event.setTime(times[i])
            event.setEvent(events[i])
            eventList.add(event)
        }

        // 通知适配器数据已更新
        adapter.notifyDataSetChanged()
    }
} 