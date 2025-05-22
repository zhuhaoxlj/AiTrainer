package com.lindum.mlkit.vision.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.lindum.mlkit.vision.MainActivity
import com.lindum.mlkit.vision.entity.PoseEntity
import com.lindum.mlkit.vision.view.PoseLibraryItem

class PoseLibraryViewModel : ViewModel() {

    // 管理加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 管理动作库数据
    private val _poseLibraryItems = MutableLiveData<List<PoseLibraryItem>>()
    val poseLibraryItems: LiveData<List<PoseLibraryItem>> = _poseLibraryItems

    // 保存已加载的数据状态
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded

    // 缓存动作库实体数据
    private val _poseEntities = MutableLiveData<List<PoseEntity>>()
    val poseEntities: LiveData<List<PoseEntity>> = _poseEntities

    // 缓存图片数据
    private val _coverImages = MutableLiveData<List<Bitmap>>()
    val coverImages: LiveData<List<Bitmap>> = _coverImages

    private val _rightImages = MutableLiveData<List<Bitmap>>()
    val rightImages: LiveData<List<Bitmap>> = _rightImages

    private val _feelingImages = MutableLiveData<List<Bitmap>>()
    val feelingImages: LiveData<List<Bitmap>> = _feelingImages

    init {
        _isLoading.value = false
        _poseLibraryItems.value = emptyList()
        _isDataLoaded.value = false
        _poseEntities.value = emptyList()
        _coverImages.value = emptyList()
        _rightImages.value = emptyList()
        _feelingImages.value = emptyList()
    }

    fun setLoading(loading: Boolean) {
        _isLoading.postValue(loading)
    }

    fun updatePoseLibraryItems(items: List<PoseLibraryItem>) {
        _poseLibraryItems.postValue(items)
    }

    fun updatePoseEntities(entities: List<PoseEntity>) {
        _poseEntities.postValue(entities)
        Log.i("test_pose_list", Gson().toJson(_poseEntities.value))
        // 将数据同步到MainActivity的静态变量中保持向后兼容
        MainActivity.Companion.poseEntityArrayList = ArrayList(entities)
    }

    fun updateCoverImages(images: List<Bitmap>) {
        _coverImages.postValue(images)
        // 同步到MainActivity的静态变量
        MainActivity.Companion.poseLibrarySwiperImageArrayList = ArrayList(images)
    }

    fun updateRightImages(images: List<Bitmap>) {
        _rightImages.postValue(images)
        // 同步到MainActivity的静态变量
        MainActivity.Companion.poseRightImageArrayList = ArrayList(images)
    }

    fun updateFeelingImages(images: List<Bitmap>) {
        _feelingImages.postValue(images)
        // 同步到MainActivity的静态变量
        MainActivity.Companion.poseDetailFeelingImageArrayList = ArrayList(images)
    }

    fun setDataLoaded(loaded: Boolean) {
        _isDataLoaded.postValue(loaded)
    }

    fun shouldLoadData(): Boolean {
        return _isDataLoaded.value != true ||
                _poseEntities.value.isNullOrEmpty() ||
                _coverImages.value.isNullOrEmpty()
    }
} 