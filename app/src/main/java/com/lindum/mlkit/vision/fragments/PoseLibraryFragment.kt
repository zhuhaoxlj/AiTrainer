package com.lindum.mlkit.vision.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.FragmentPoseLibraryBinding
import com.lindum.mlkit.vision.entity.PoseEntity
import com.lindum.mlkit.vision.view.HomeAdapter
import com.lindum.mlkit.vision.view.PoseDetailsActivity
import com.lindum.mlkit.vision.view.PoseLibraryItem
import com.lindum.mlkit.vision.viewmodels.PoseLibraryViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import com.lindum.mlkit.vision.utils.StatusBarUtils

class PoseLibraryFragment : Fragment(R.layout.fragment_pose_library), HomeAdapter.ItemListener {

    private lateinit var adapter: HomeAdapter
    private val poseLibraryItems = ArrayList<PoseLibraryItem>()
    private lateinit var binding: FragmentPoseLibraryBinding
    private var loadingPlaceHolderBitmap: Bitmap? = null
    private lateinit var mViewModel: PoseLibraryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPoseLibraryBinding.bind(view)
        val layoutParams = binding.vFakeStatusBar.layoutParams
        layoutParams.height = StatusBarUtils.getStatusBarHeight(requireContext())
        binding.vFakeStatusBar.layoutParams = layoutParams
        // 确保占位图正确初始化
        try {
            loadingPlaceHolderBitmap = BitmapFactory.decodeResource(resources, R.drawable.loading3)
            if (loadingPlaceHolderBitmap == null) {
                Logger.e("占位图初始化失败，创建默认1x1占位图")
                loadingPlaceHolderBitmap = createBitmap(1, 1)
            }
        } catch (e: Exception) {
            Logger.e("初始化占位图失败: ${e.message}")
            loadingPlaceHolderBitmap = createBitmap(1, 1)
        }

        // 使用activity范围的ViewModel，确保数据在Fragment之间共享
        mViewModel = ViewModelProvider(requireActivity()).get(PoseLibraryViewModel::class.java)

        initView()
        observeViewModel()

        // 只在需要时加载数据
        checkAndLoadData()
    }

    private fun observeViewModel() {
        mViewModel.poseLibraryItems.observe(viewLifecycleOwner) { items ->
            poseLibraryItems.clear()
            poseLibraryItems.addAll(items)
            adapter.notifyDataSetChanged()
        }

        mViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun initView() {
        // 初始化RecyclerView
        adapter = HomeAdapter(requireContext(), poseLibraryItems, this)
        binding.recyclerView.adapter = adapter

        val gridLayoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = gridLayoutManager
    }

    private fun checkAndLoadData() {
        // 只在ViewModel中没有数据时加载
        if (mViewModel.poseLibraryItems.value.isNullOrEmpty()) {
            loadPoseLibraryData()
        }
    }

    private fun loadPoseLibraryData() {
        // 标记开始加载
        mViewModel.setLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 从assets文件夹读取JSON数据
                val jsonString = requireContext().assets.open("pose_list.json").bufferedReader()
                    .use { it.readText() }
                val gson = Gson()
                val type = object : TypeToken<List<PoseEntity>>() {}.type
                val poseEntities = gson.fromJson<List<PoseEntity>>(jsonString, type)
                // 更新ViewModel中的实体数据
                mViewModel.updatePoseEntities(poseEntities)

                // 设置动作持续变量类型
                if (poseEntities.isNotEmpty()) {

                    // 加载图片
                    loadImages()

                    // 切换到主线程更新UI
                    withContext(Dispatchers.Main) {
                        // 更新UI
                        updateLibraryItemsUI()
                    }
                }

                // 标记数据已加载
                mViewModel.setDataLoaded(true)

            } catch (e: Exception) {
                Logger.e("加载动作库数据失败: ${e.message}")
            } finally {
                // 标记加载结束
                withContext(Dispatchers.Main) {
                    mViewModel.setLoading(false)
                }
            }
        }
    }

    private suspend fun loadImages() {
        // 准备新的图片数据
        val coverImages = ArrayList<Bitmap>()
        val feelingImages = ArrayList<Bitmap>()
        val rightImages = ArrayList<Bitmap>()  // 添加正确姿势图片列表

        try {
            // 检查URL数组是否为空
            if (MainActivity.Companion.poseLibraryCoverImageUrl.isEmpty()) {
                Logger.e("封面图片URL数组为空")
                return
            }

            // 预先为所有数组添加占位图，确保即使加载失败也有内容展示
            for (i in MainActivity.Companion.poseLibraryCoverImageUrl.indices) {
                coverImages.add(loadingPlaceHolderBitmap!!)
                feelingImages.add(loadingPlaceHolderBitmap!!)
                rightImages.add(loadingPlaceHolderBitmap!!)  // 为正确姿势图片添加占位图
            }

            // 在IO线程上先一次性更新空的图片集合并显示占位图
            withContext(Dispatchers.Main) {
                mViewModel.updateCoverImages(coverImages)
                mViewModel.updateFeelingImages(feelingImages)
                mViewModel.updateRightImages(rightImages)  // 更新正确姿势图片
                // 先使用占位图更新UI，让用户看到内容
                updateLibraryItemsUI()
            }

            // 使用协程的异步特性加载实际图片
            val loadJobs = MainActivity.Companion.poseLibraryCoverImageUrl.indices.map { i ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        // 封面图
                        val coverBitmap = BitmapFactory.decodeStream(
                            requireContext().assets.open(
                                MainActivity.Companion.poseLibraryCoverImageUrl[i]
                                    .replace("file:///android_asset/", "")
                            )
                        )
                        coverImages[i] = coverBitmap

                        // 正确姿势图
                        val rightBitmap = BitmapFactory.decodeStream(
                            requireContext().assets.open(
                                MainActivity.Companion.rightPoseImageUrl[i]
                                    .replace("file:///android_asset/", "")
                            )
                        )
                        rightImages[i] = rightBitmap  // 保存正确姿势图片

                        // 感受图
                        try {
                            val feelingBitmap = BitmapFactory.decodeStream(
                                requireContext().assets.open(
                                    MainActivity.Companion.poseFeelingImageUrl[i]
                                        .replace("file:///android_asset/", "")
                                )
                            )
                            feelingImages[i] = feelingBitmap
                        } catch (e: Exception) {
                            Logger.e("加载感受图片失败: ${e.message}")
                        }

                        // 每当有一个图片加载完成，就更新一次UI
                        withContext(Dispatchers.Main) {
                            mViewModel.updateCoverImages(ArrayList(coverImages))
                            mViewModel.updateFeelingImages(ArrayList(feelingImages))
                            mViewModel.updateRightImages(ArrayList(rightImages))  // 更新正确姿势图片
                            updateLibraryItemsUI()
                        }
                    } catch (e: Exception) {
                        Logger.e("图片加载失败: ${e.message}")
                    }
                }
            }

            // 等待所有图片加载完成
            loadJobs.forEach { it.join() }

        } catch (e: Exception) {
            Logger.e("加载图片过程失败: ${e.message}")
        }
    }

    private suspend fun loadBitmapWithGlide(url: String): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                if (url.isBlank()) {
                    Logger.e("图片URL为空")
                    return@withContext loadingPlaceHolderBitmap!!
                }

                // 直接从Assets读取位图，而不是使用Glide的阻塞式加载
                if (url.startsWith("file:///android_asset/")) {
                    val assetPath = url.replace("file:///android_asset/", "")
                    try {
                        return@withContext BitmapFactory.decodeStream(
                            requireContext().assets.open(assetPath)
                        )
                    } catch (e: Exception) {
                        Logger.e("从assets加载图片失败: $assetPath - ${e.message}")
                        return@withContext loadingPlaceHolderBitmap!!
                    }
                }

                // 对于非asset资源，仍然使用Glide但采用非阻塞方式
                val target = Glide.with(requireContext())
                    .asBitmap()
                    .load(url)
                    .apply(
                        RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.loading3)
                            .error(R.drawable.loading3)
                    )
                    .submit(300, 300)

                try {
                    // 设置较短的超时时间以避免长时间阻塞
                    return@withContext target.get(5, java.util.concurrent.TimeUnit.SECONDS)
                } catch (e: Exception) {
                    Logger.e("Glide加载图片超时: $url - ${e.message}")
                    return@withContext loadingPlaceHolderBitmap!!
                }
            } catch (e: Exception) {
                Logger.e("Glide加载图片失败: $url - ${e.message}")
                // 确保返回有效的占位图
                return@withContext loadingPlaceHolderBitmap!!
            }
        }
    }

    private suspend fun updateLibraryItemsUI() {
        val newItems = ArrayList<PoseLibraryItem>()

        // 获取数据，如果为空则使用空列表代替null
        val poseList = mViewModel.poseEntities.value ?: ArrayList()
        val coverImages = mViewModel.coverImages.value ?: ArrayList()

        if (poseList.isEmpty()) {
            Logger.e("姿势数据为空，无法显示列表项")
        }

        if (coverImages.isEmpty()) {
            Logger.e("封面图片为空，使用占位图显示")
        }

        // 取两个列表中较小的大小，确保不会发生索引越界
        val size = minOf(poseList.size, coverImages.size)
        for (i in 0 until size) {
            try {
                val poseEntity = poseList[i]
                // 如果对应索引的图片不存在，使用占位图
                val coverBitmap =
                    if (i < coverImages.size) coverImages[i] else loadingPlaceHolderBitmap!!

                newItems.add(
                    PoseLibraryItem(
                        i,
                        poseEntity.poseName,
                        "",
                        coverBitmap,
                        "#000000"
                    )
                )
            } catch (e: Exception) {
                Logger.e("创建列表项失败 索引:$i 错误:${e.message}")
            }
        }

        // 确保UI总是更新，即使列表为空也发送更新通知
        mViewModel.updatePoseLibraryItems(newItems)
    }

    override fun onItemClick(item: PoseLibraryItem) {
        val poseList = mViewModel.poseEntities.value ?: return

        if (item.index < poseList.size) {
            val intent = Intent(activity, PoseDetailsActivity::class.java)
            intent.putExtra("feeling", poseList[item.index].poseFeeling)
            intent.putExtra("steps", poseList[item.index].stepsOfPose)
            intent.putExtra("commonMistakes", poseList[item.index].commonMistakes)
            intent.putExtra("details", poseList[item.index].detailTips)
            intent.putExtra("breath", poseList[item.index].breathTips)
            intent.putExtra("poseIndex", item.index)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        Glide.with(this).clear(binding.recyclerView)
    }
}