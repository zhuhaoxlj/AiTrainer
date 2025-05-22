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
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.databinding.FragmentPoseLibraryBinding
import com.lindum.mlkit.vision.sql.ControlTable
import com.lindum.mlkit.vision.utils.StatusBarUtils
import com.lindum.mlkit.vision.view.HomeAdapter
import com.lindum.mlkit.vision.view.PoseDetailsActivity
import com.lindum.mlkit.vision.view.PoseLibraryItem
import com.lindum.mlkit.vision.viewmodels.PoseLibraryViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                loadingPlaceHolderBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        } catch (e: Exception) {
            Logger.e("初始化占位图失败: ${e.message}")
            loadingPlaceHolderBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
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
                // 从数据库加载动作库数据
                val controlTable = ControlTable()
                val poseEntities = controlTable.readTable() ?: return@launch

                // 更新ViewModel中的实体数据
                mViewModel.updatePoseEntities(poseEntities)

                // 设置动作持续变量类型
                if (poseEntities.isNotEmpty()) {
                    for (poseEntity in poseEntities) {
                        // 倒计时类型0 计数类型1
                        poseEntity.setPoseType(0)
                        poseEntity.setPoseProcessValue(30)
                    }

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
        val rightImages = ArrayList<Bitmap>()
        val feelingImages = ArrayList<Bitmap>()

        for (i in MainActivity.Companion.poseLibraryCoverImageUrl.indices) {
            try {
                // 使用协程加载图片
                val coverBitmap =
                    loadBitmapWithGlide(MainActivity.Companion.poseLibraryCoverImageUrl[i])
                val rightBitmap = loadBitmapWithGlide(MainActivity.Companion.rightPoseImageUrl[i])

                coverImages.add(coverBitmap)
                rightImages.add(rightBitmap)

                try {
                    val feelingBitmap =
                        loadBitmapWithGlide(MainActivity.Companion.poseFeelingImageUrl[i].toString())
                    feelingImages.add(feelingBitmap)
                } catch (e: Exception) {
                    Logger.e("加载感受图片失败: ${e.message}")
                    feelingImages.add(loadingPlaceHolderBitmap!!)
                }

            } catch (e: Exception) {
                Logger.e("图片加载失败: ${e.message}")
                // 确保即使加载失败也添加占位图，保持图片集合大小一致
                coverImages.add(loadingPlaceHolderBitmap!!)
                rightImages.add(loadingPlaceHolderBitmap!!)
                feelingImages.add(loadingPlaceHolderBitmap!!)
            }
        }

        // 检查图片集合，确保至少有一些数据
        if (coverImages.isEmpty()) {
            Logger.e("封面图片集合为空，将添加默认占位图")
            coverImages.add(loadingPlaceHolderBitmap!!)
        }

        if (rightImages.isEmpty()) {
            Logger.e("正确姿势图片集合为空，将添加默认占位图")
            rightImages.add(loadingPlaceHolderBitmap!!)
        }

        if (feelingImages.isEmpty()) {
            Logger.e("感受图片集合为空，将添加默认占位图")
            feelingImages.add(loadingPlaceHolderBitmap!!)
        }

        // 在IO线程上一次性更新所有图片集合
        mViewModel.updateCoverImages(coverImages)
        mViewModel.updateRightImages(rightImages)
        mViewModel.updateFeelingImages(feelingImages)
    }

    private suspend fun loadBitmapWithGlide(url: String): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                if (url.isBlank()) {
                    Logger.e("图片URL为空")
                    return@withContext loadingPlaceHolderBitmap!!
                }

                Glide.with(requireContext())
                    .asBitmap()
                    .load(url)
                    .apply(
                        RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.loading3)
                            .error(R.drawable.loading3)
                            .timeout(10000) // 设置10秒超时
                    )
                    .submit()
                    .get()
            } catch (e: Exception) {
                Logger.e("Glide加载图片失败: $url - ${e.message}")
                // 确保返回有效的占位图
                if (loadingPlaceHolderBitmap == null) {
                    try {
                        return@withContext BitmapFactory.decodeResource(
                            resources,
                            R.drawable.loading3
                        )
                    } catch (e2: Exception) {
                        Logger.e("加载占位图失败: ${e2.message}")
                        // 创建一个简单的1x1像素图
                        return@withContext Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    }
                } else {
                    return@withContext loadingPlaceHolderBitmap!!
                }
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
                        poseEntity.getPoseName(),
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
        if (newItems.isEmpty()) {
            Logger.e("更新后的姿势列表为空，请检查数据源")
        } else {
            Logger.d("成功更新姿势列表项，共${newItems.size}项")
        }
    }

    override fun onItemClick(item: PoseLibraryItem) {
        val poseList = mViewModel.poseEntities.value ?: return

        if (item.index < poseList.size) {
            val intent = Intent(activity, PoseDetailsActivity::class.java)
            intent.putExtra("feeling", poseList[item.index].getPoseFeeling())
            intent.putExtra("steps", poseList[item.index].getStepsOfPose())
            intent.putExtra("commonMistakes", poseList[item.index].getCommonMistakes())
            intent.putExtra("details", poseList[item.index].getDetailTips())
            intent.putExtra("breath", poseList[item.index].getBreathTips())
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