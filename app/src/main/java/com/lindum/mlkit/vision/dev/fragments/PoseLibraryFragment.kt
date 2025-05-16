package com.lindum.mlkit.vision.dev.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lindum.mlkit.vision.dev.MainActivity
import com.lindum.mlkit.vision.dev.R
import com.lindum.mlkit.vision.dev.view.HomeAdapter
import com.lindum.mlkit.vision.dev.view.PoseDetailsActivity
import com.lindum.mlkit.vision.dev.view.PoseLibraryItem
import com.orhanobut.logger.Logger
import com.xuexiang.xui.utils.StatusBarUtils
import com.xuexiang.xui.widget.actionbar.TitleBar
import java.util.ArrayList

class PoseLibraryFragment : Fragment(), HomeAdapter.ItemListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter
    private var rootView: View? = null
    private val poseLibraryItems = ArrayList<PoseLibraryItem>()
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.pose_library, container, false)
            initView()
        }
        return rootView
    }
    
    private fun initView() {
        rootView?.let { view ->
            // 去除动作库标题栏左侧图标
            val titleBar = view.findViewById<TitleBar>(R.id.poseLibraryTitleBar)
            titleBar.setLeftVisible(false)
            
            // 初始化RecyclerView
            recyclerView = view.findViewById(R.id.recyclerView)
            adapter = HomeAdapter(requireContext(), poseLibraryItems, this)
            recyclerView.adapter = adapter
            
            val gridLayoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = gridLayoutManager
            
            // 加载动作库数据
            loadPoseLibraryData()
        }
    }
    
    fun refreshData() {
        loadPoseLibraryData()
    }
    
    private fun loadPoseLibraryData() {
        // 清空现有数据
        poseLibraryItems.clear()
        
        // 从MainActivity获取动作库数据
        (activity as? MainActivity)?.let { mainActivity ->
            MainActivity.poseEntityArrayList?.let { poseList ->
                if (poseList.isNotEmpty() && MainActivity.poseLibrarySwiperImageArrayList.isNotEmpty()) {
                    for (poseEntity in poseList) {
                        val index = poseList.indexOf(poseEntity)
                        if (index < MainActivity.poseLibrarySwiperImageArrayList.size) {
                            poseLibraryItems.add(
                                PoseLibraryItem(
                                    index,
                                    poseEntity.getPoseName(),
                                    "",
                                    MainActivity.poseLibrarySwiperImageArrayList[index],
                                    "#000000"
                                )
                            )
                        }
                    }
                    // 通知适配器数据已更新
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
    
    override fun onItemClick(item: PoseLibraryItem) {
        Toast.makeText(context, item.title + "第" + item.index + "个", Toast.LENGTH_SHORT).show()
        
        MainActivity.poseEntityArrayList?.let { poseList ->
            if (item.index < poseList.size) {
                val intent = Intent(activity, PoseDetailsActivity::class.java)
                intent.putExtra("feeling", poseList[item.index].getPoseFeeling())
                intent.putExtra("steps", poseList[item.index].getStepsOfPose())
                intent.putExtra("commonMistakes", poseList[item.index].getCommonMistakes())
                intent.putExtra("details", poseList[item.index].getDetailTips())
                intent.putExtra("breath", poseList[item.index].getBreathTips())
                intent.putExtra("poseIndex", item.index)
                Logger.e(poseList[item.index].getPoseSwiperImageUrl()[0])
                startActivity(intent)
            }
        }
    }
} 