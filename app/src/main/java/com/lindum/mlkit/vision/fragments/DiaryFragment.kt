package com.lindum.mlkit.vision.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.FragmentTrainDiaryBinding
import com.lindum.mlkit.vision.result.DotTimeLineAdapter
import com.lindum.mlkit.vision.result.Event
import com.lindum.mlkit.vision.utils.FileUtils
import com.lindum.mlkit.vision.view.BubbleView
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryFragment : Fragment(R.layout.fragment_train_diary) {

    private lateinit var adapter: DotTimeLineAdapter
    private lateinit var itemDecoration: DotItemDecoration
    private lateinit var binding: FragmentTrainDiaryBinding

    private val TAG = "DiaryFragment"

    // 缓存数据
    private val eventList = ArrayList<Event?>()

    // 分页加载参数
    private val PAGE_SIZE = 30
    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true

    // 预加载标志
    private var isPreloaded = false

    // 用于追踪异步任务
    private var preloadJob: Job? = null
    private var decorationSetupJob: Job? = null
    private var loadDataJob: Job? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: start")
        val startTime = System.currentTimeMillis()
        binding = FragmentTrainDiaryBinding.bind(view)

        // 初始化基本视图结构，但延迟复杂操作
        setupBasicViews()

        // 延迟加载装饰和预加载数据，优先显示UI
        lifecycleScope.launch {
            delay(100) // 给UI主线程一点时间来渲染基本视图
            setupTimelineDecoration()
            preloadData()
        }

        Log.d(
            TAG,
            "onViewCreated: basic setup done, took ${System.currentTimeMillis() - startTime}ms"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: start")
        val startTime = System.currentTimeMillis()

        // 每次恢复可见状态时都重新加载第一页数据，确保显示最新记录
        loadFirstPageData()

        Log.d(TAG, "onResume: end, took ${System.currentTimeMillis() - startTime}ms")
    }

    override fun onPause() {
        super.onPause()
        // 取消可能正在进行的预加载任务
        preloadJob?.cancel()
        decorationSetupJob?.cancel()
    }

    // 预加载数据，可以在应用启动时调用
    fun preloadData() {
        if (isPreloaded || preloadJob != null) return

        preloadJob = lifecycleScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "preloadData: start")

            // 预加载第一页数据
            val records = FileUtils.readFileToStringPaged(
                "${MainActivity.Companion.appFilePath}/files/analysisReport/",
                "recordFile.sys",
                PAGE_SIZE,
                0
            )
            Log.d(TAG, "preloadData: file read took ${System.currentTimeMillis() - startTime}ms")
            if (records.isNotEmpty()) {
                // 处理数据但不更新UI
                val processStartTime = System.currentTimeMillis()
                processRecords(records)
                Log.d(
                    TAG,
                    "preloadData: processing took ${System.currentTimeMillis() - processStartTime}ms"
                )
                isPreloaded = true
            }
            Log.d(TAG, "preloadData: end, total time ${System.currentTimeMillis() - startTime}ms")
            preloadJob = null
        }
    }

    // 设置基本视图，快速完成以显示界面
    private fun setupBasicViews() {
        val startTime = System.currentTimeMillis()

        try {
            val statusBarStartTime = System.currentTimeMillis()
            // 修复 StatusBarUtils 问题，使用安全的方式获取状态栏高度
            val statusBarHeight = getStatusBarHeightSafely()
            val layoutParams = binding.vFakeStatusBar.layoutParams
            layoutParams.height = statusBarHeight
            binding.vFakeStatusBar.layoutParams = layoutParams
        } catch (e: Exception) {
        }

        System.currentTimeMillis()
        // 使用更高效的布局管理器配置
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        // 优化RecyclerView配置
        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            // 增加视图池大小以提高性能
            recycledViewPool.setMaxRecycledViews(0, 20)

            // 关闭动画以提高性能
            itemAnimator = null
        }

        // 初始化适配器 - 这一步必须在主线程完成
        adapter = DotTimeLineAdapter(requireContext(), eventList)
        binding.recyclerView.adapter = adapter

        // 添加滚动监听，实现上拉加载更多
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    (visibleItemCount + lastVisibleItemPosition >= totalItemCount - 5)
                ) {
                    loadMoreData()
                }
            }
        })

    }

    // 设置时间线装饰 - 这一步可能比较耗时，所以延迟执行
    private fun setupTimelineDecoration() {
        if (decorationSetupJob != null) return

        decorationSetupJob = lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "setupTimelineDecoration: start")

            // 在后台线程创建装饰对象
            val decoration = withContext(Dispatchers.Default) {
                DotItemDecoration.Builder(requireContext())
                    .setOrientation(DotItemDecoration.VERTICAL)
                    .setItemStyle(DotItemDecoration.STYLE_DRAW)
                    .setTopDistance(20f) // dp
                    .setItemInterVal(10f) // dp
                    .setItemPaddingLeft(10f)
                    .setItemPaddingRight(10f)
                    .setDotColor("#FFFFFF".toColorInt())
                    .setDotRadius(4) // dp
                    .setDotPaddingTop(20)
                    .setDotInItemOrientationCenter(false)
                    .setLineColor("#FFFFFF".toColorInt())
                    .setLineWidth(1f) // dp
                    .setEndText("终")
                    .setTextColor(Color.WHITE)
                    .setTextSize(12f) // sp
                    .setDotPaddingText(15f)
                    .setBottomDistance(40f)
                    .create()
            }

            // 切换回主线程应用装饰
            withContext(Dispatchers.Main) {
                itemDecoration = decoration

                itemDecoration.setSpanIndexListener { view, spanIndex ->
                    val bubbleView = view.findViewById<BubbleView>(R.id.bv_background)
                    bubbleView.setArrow(spanIndex)
                }

                if (binding.recyclerView.itemDecorationCount == 0) {
                    binding.recyclerView.addItemDecoration(itemDecoration)
                }
            }

            Log.d(
                TAG,
                "setupTimelineDecoration: end, total time ${System.currentTimeMillis() - startTime}ms"
            )
            decorationSetupJob = null
        }
    }

    // 安全获取状态栏高度的方法
    private fun getStatusBarHeightSafely(): Int {
        val resources = resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // 如果无法获取，使用默认值（25dp转为像素）
            val scale = resources.displayMetrics.density
            (25 * scale + 0.5f).toInt()
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

    private fun loadFirstPageData() {
        Log.d(TAG, "loadFirstPageData: start")
        val startTime = System.currentTimeMillis()

        // 如果已经在加载中，取消之前的加载任务
        loadDataJob?.cancel()

        // 显示进度条
        binding.progressBar.visibility = View.VISIBLE
        isLoading = true

        // 清空现有数据
        eventList.clear()
        currentPage = 0

        // 异步加载第一页数据
        loadDataJob = lifecycleScope.launch {
            val loadStartTime = System.currentTimeMillis()

            // 使用async以便可以同时加载数据和设置装饰
            val dataDeferred = async(Dispatchers.IO) {
                // 确保清除文件缓存，强制从文件读取最新数据
                FileUtils.clearFileCache()
                loadPagedTrainingHistory(currentPage)
            }

            // 确保装饰已设置完成
            if (binding.recyclerView.itemDecorationCount == 0) {
                setupTimelineDecoration()
            }

            // 等待数据加载完成
            val result = dataDeferred.await()
            Log.d(
                TAG,
                "loadFirstPageData: data loading took ${System.currentTimeMillis() - loadStartTime}ms"
            )

            withContext(Dispatchers.Main) {
                val uiUpdateStartTime = System.currentTimeMillis()
                // 更新UI
                eventList.addAll(result)
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                isLoading = false
                Log.d(
                    TAG,
                    "loadFirstPageData: UI update took ${System.currentTimeMillis() - uiUpdateStartTime}ms"
                )

                // 更新是否还有更多数据的标志
                hasMoreData = result.size >= PAGE_SIZE

                // 预加载下一页
                if (hasMoreData) {
                    preloadNextPage()
                }
            }
            Log.d(
                TAG,
                "loadFirstPageData: end, total time ${System.currentTimeMillis() - startTime}ms"
            )
            loadDataJob = null
        }
    }

    private fun loadMoreData() {
        if (isLoading || !hasMoreData || loadDataJob != null) return

        Log.d(TAG, "loadMoreData: start page ${currentPage + 1}")
        val startTime = System.currentTimeMillis()

        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        currentPage++

        loadDataJob = lifecycleScope.launch {
            val loadStartTime = System.currentTimeMillis()
            val result = loadPagedTrainingHistory(currentPage)
            Log.d(
                TAG,
                "loadMoreData: data loading took ${System.currentTimeMillis() - loadStartTime}ms"
            )

            withContext(Dispatchers.Main) {
                val uiUpdateStartTime = System.currentTimeMillis()
                // 更新UI
                val startPosition = eventList.size
                eventList.addAll(result)
                adapter.notifyItemRangeInserted(startPosition, result.size)
                binding.progressBar.visibility = View.GONE
                isLoading = false
                Log.d(
                    TAG,
                    "loadMoreData: UI update took ${System.currentTimeMillis() - uiUpdateStartTime}ms"
                )

                // 更新是否还有更多数据的标志
                hasMoreData = result.size >= PAGE_SIZE

                // 预加载下一页
                if (hasMoreData) {
                    preloadNextPage()
                }
            }
            Log.d(TAG, "loadMoreData: end, total time ${System.currentTimeMillis() - startTime}ms")
            loadDataJob = null
        }
    }

    private fun preloadNextPage() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "preloadNextPage: start for page ${currentPage + 1}")
            val startTime = System.currentTimeMillis()
            // 预加载下一页，但不更新UI
            loadPagedTrainingHistory(currentPage + 1)
            Log.d(TAG, "preloadNextPage: end, took ${System.currentTimeMillis() - startTime}ms")
        }
    }

    private suspend fun loadPagedTrainingHistory(page: Int): List<Event?> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "loadPagedTrainingHistory: start for page $page")
            val startTime = System.currentTimeMillis()

            val result = ArrayList<Event?>()

            // 使用分页读取方法
            val fileStartTime = System.currentTimeMillis()
            val records = FileUtils.readFileToStringPaged(
                "${MainActivity.Companion.appFilePath}/files/analysisReport/",
                "recordFile.sys",
                PAGE_SIZE,
                page
            )
            Log.d(
                TAG,
                "loadPagedTrainingHistory: file reading took ${System.currentTimeMillis() - fileStartTime}ms"
            )

            // 处理记录
            val processStartTime = System.currentTimeMillis()
            val processedRecords = processRecords(records)
            result.addAll(processedRecords)
            Log.d(
                TAG,
                "loadPagedTrainingHistory: processing took ${System.currentTimeMillis() - processStartTime}ms"
            )

            Log.d(
                TAG,
                "loadPagedTrainingHistory: end, total time ${System.currentTimeMillis() - startTime}ms"
            )
            return@withContext result
        }

    private fun processRecords(records: ArrayList<String>): List<Event?> {
        Log.d(TAG, "processRecords: start with ${records.size} records")
        val startTime = System.currentTimeMillis()

        val result = ArrayList<Event?>()

        // 按时间戳降序排序
        val sortStartTime = System.currentTimeMillis()
        val sortedRecords = records.mapNotNull { record ->
            val parts = record.split(" ", limit = 2)
            if (parts.size >= 2) {
                Pair(parts[0].toLong(), parts[1])
            } else null
        }.sortedByDescending { it.first }
        Log.d(TAG, "processRecords: sorting took ${System.currentTimeMillis() - sortStartTime}ms")

        // 创建事件对象
        val createStartTime = System.currentTimeMillis()
        for ((time, event) in sortedRecords) {
            val eventObj = Event()
            eventObj.setTime(time)
            eventObj.setEvent(event)
            result.add(eventObj)
        }
        Log.d(
            TAG,
            "processRecords: creating events took ${System.currentTimeMillis() - createStartTime}ms"
        )

        Log.d(TAG, "processRecords: end, total time ${System.currentTimeMillis() - startTime}ms")
        return result
    }
} 