package com.lindum.mlkit.vision.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.Collections

class DiaryFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DotTimeLineAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var itemDecoration: DotItemDecoration
    
    // 缓存数据
    private val eventList = ArrayList<Event?>()
    
    // 分页加载参数
    private val PAGE_SIZE = 30
    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true
    
    // 预加载标志
    private var isPreloaded = false

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
        // 如果数据为空，才加载第一页数据
        if (eventList.isEmpty()) {
            loadFirstPageData()
        }
    }
    
    // 预加载数据，可以在应用启动时调用
    @RequiresApi(Build.VERSION_CODES.O)
    fun preloadData() {
        if (isPreloaded) return
        
        lifecycleScope.launch(Dispatchers.IO) {
            // 预加载第一页数据
            val records = FileUtils.readFileToStringPaged(
                "${MainActivity.Companion.appFilePath}/files/analysisReport/",
                "recordFile.sys",
                PAGE_SIZE,
                0
            )
            
            if (records.isNotEmpty()) {
                // 处理数据但不更新UI
                processRecords(records)
                isPreloaded = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initView() {
        rootView?.let { view ->
            // 状态栏背景条
            val statusBar = view.findViewById<View>(R.id.v_fake_status_bar)
            val params = statusBar.layoutParams
            params.height = StatusBarUtils.getStatusBarHeight(requireContext())
            statusBar.layoutParams = params

            // 添加进度条
            progressBar = view.findViewById(R.id.progress_bar)
            if (progressBar == null) {
                // 如果布局中没有进度条，动态创建一个
                progressBar = ProgressBar(context)
                progressBar.id = View.generateViewId()
                (view as ViewGroup).addView(progressBar)
                val layoutParams = progressBar.layoutParams as ViewGroup.LayoutParams
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                progressBar.layoutParams = layoutParams
                progressBar.visibility = View.GONE
            }

            // 初始化RecyclerView
            recyclerView = view.findViewById(R.id.recycler_view)
            
            // 使用更高效的布局管理器配置
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            recyclerView.layoutManager = layoutManager
            
            // 设置RecyclerView优化选项
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(20)

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

            // 添加滚动监听，实现上拉加载更多
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    
                    // 获取最后一个可见项的位置
                    val lastVisibleItems = IntArray(layoutManager.spanCount)
                    layoutManager.findLastVisibleItemPositions(lastVisibleItems)
                    val lastVisibleItemPosition = findMax(lastVisibleItems)
                    
                    // 当滚动到底部且不在加载中且还有更多数据时，加载更多数据
                    if (!isLoading && hasMoreData && 
                        (visibleItemCount + lastVisibleItemPosition >= totalItemCount - 5)) {
                        loadMoreData()
                    }
                }
            })
            
            // 尝试预加载数据
            preloadData()
        }
    }
    
    private fun findMax(lastPositions: IntArray): Int {
        var max = lastPositions[0]
        for (value in lastPositions) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFirstPageData() {
        // 显示进度条
        progressBar.visibility = View.VISIBLE
        isLoading = true
        
        // 清空现有数据
        eventList.clear()
        currentPage = 0
        
        // 异步加载第一页数据
        lifecycleScope.launch {
            val result = loadPagedTrainingHistory(currentPage)
            withContext(Dispatchers.Main) {
                // 更新UI
                eventList.addAll(result)
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
                isLoading = false
                
                // 更新是否还有更多数据的标志
                hasMoreData = result.size >= PAGE_SIZE
                
                // 预加载下一页
                if (hasMoreData) {
                    preloadNextPage()
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMoreData() {
        if (isLoading || !hasMoreData) return
        
        isLoading = true
        progressBar.visibility = View.VISIBLE
        
        currentPage++
        
        lifecycleScope.launch {
            val result = loadPagedTrainingHistory(currentPage)
            withContext(Dispatchers.Main) {
                // 更新UI
                val startPosition = eventList.size
                eventList.addAll(result)
                adapter.notifyItemRangeInserted(startPosition, result.size)
                progressBar.visibility = View.GONE
                isLoading = false
                
                // 更新是否还有更多数据的标志
                hasMoreData = result.size >= PAGE_SIZE
                
                // 预加载下一页
                if (hasMoreData) {
                    preloadNextPage()
                }
            }
        }
    }
    
    private fun preloadNextPage() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 预加载下一页，但不更新UI
            loadPagedTrainingHistory(currentPage + 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadPagedTrainingHistory(page: Int): List<Event?> = withContext(Dispatchers.IO) {
        val result = ArrayList<Event?>()
        
        // 使用分页读取方法
        val records = FileUtils.readFileToStringPaged(
            "${MainActivity.Companion.appFilePath}/files/analysisReport/",
            "recordFile.sys",
            PAGE_SIZE,
            page
        )
        
        // 处理记录
        val processedRecords = processRecords(records)
        result.addAll(processedRecords)
        
        return@withContext result
    }
    
    private fun processRecords(records: ArrayList<String>): List<Event?> {
        val result = ArrayList<Event?>()
        
        // 按时间戳降序排序
        val sortedRecords = records.mapNotNull { record ->
            val parts = record.split(" ", limit = 2)
            if (parts.size >= 2) {
                Pair(parts[0].toLong(), parts[1])
            } else null
        }.sortedByDescending { it.first }
        
        // 创建事件对象
        for ((time, event) in sortedRecords) {
            val eventObj = Event()
            eventObj.setTime(time)
            eventObj.setEvent(event)
            result.add(eventObj)
        }
        
        return result
    }
} 