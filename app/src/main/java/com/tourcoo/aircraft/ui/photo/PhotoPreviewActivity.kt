package com.tourcoo.aircraft.ui.photo

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.apkfuns.logutils.LogUtils
import com.github.chrisbanes.photoview.PhotoView
import com.tourcoo.aircraft.product.AircraftUtil
import com.tourcoo.aircraft.product.ProductManager
import com.tourcoo.aircraft.ui.photo.LiveDataConstantOld.liveData
import com.tourcoo.aircraftmanager.R
import com.tourcoo.util.DateUtil
import com.tourcoo.util.GlideManager
import com.tourcoo.util.StringUtil
import com.tourcoo.util.ToastUtil
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import dji.common.error.DJIError
import dji.common.util.CommonCallbacks
import dji.sdk.camera.Camera
import dji.sdk.media.FetchMediaTask
import dji.sdk.media.FetchMediaTaskContent
import dji.sdk.media.MediaFile
import dji.sdk.media.MediaManager
import dji.sdk.media.MediaManager.FileListState
import dji.sdk.media.MediaManager.FileListStateListener
import kotlinx.android.synthetic.main.activity_photo_preview.*
import java.util.*
import kotlin.collections.ArrayList

/**
 *@description : 图片预览
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年04月22日14:24
 * @Email: 971613168@qq.com
 */
class PhotoPreviewActivity : RxAppCompatActivity() {
    private var mActivity: Activity? = null
    private var adapter: PhotoPreViewAdapter? = null
    private var position: Int? = null
    private var mediaList: MutableList<MediaFile> = ArrayList()
    private val itemViewList: ArrayList<View> = ArrayList()
    private val imageList: ArrayList<PhotoView> = ArrayList()
    private var mediaManager: MediaManager? = null
    private var mediaCreateTime: Long? = null
    private var stateListener: FileListStateListener? = null
    private var mFileListState: FileListState? = null
    private val previewMediaList: MutableList<MediaFile> = ArrayList()
    private val previewTaskList: MutableList<FetchMediaTask> = ArrayList()

    companion object {
        const val EXTRA_MEDIA_POSITION = "EXTRA_MEDIA_POSITION"
        const val EXTRA_MEDIA_LIST = "EXTRA_MEDIA_LIST"
        const val EXTRA_CREATE_TIME = "EXTRA_CREATE_TIME"
        const val REQUEST_CODE_PREVIEW = 1100
        const val TAG = "PhotoPreviewActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
        mActivity = this
        mediaCreateTime = intent?.getLongExtra(EXTRA_CREATE_TIME, -1)
        hideNavigation()
        initMediaManager2()
        liveData?.observe(this, object : Observer<MutableList<MediaFile>?> {
            override fun onChanged(it: MutableList<MediaFile>?) {
                mediaList.clear()
                if (it == null) {
                    return
                }
                mediaList.addAll(it)
                val position = findMediaPosition(it)
                if (position < 0) {
                    ToastUtil.showFailedDebug("未获取到预览")
                    return
                }
                initAdapter()
                vpPhoto.currentItem = position
            }

        })


    }

    private fun initAdapter() {
        runOnUiThread {
            if (adapter == null) {
                adapter = PhotoPreViewAdapter(itemViewList, mediaList)
                val size = mediaList.size
                imageList.clear()
                itemViewList.clear()
                for (i in 0 until size) {
                    val view = LayoutInflater.from(mActivity).inflate(R.layout.item_photo_preview, null)
                    val photoView = view.findViewById<PhotoView>(R.id.photoPreview)
                    itemViewList.add(view)
                    imageList.add(photoView)
                }
                vpPhoto!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    }

                    override fun onPageSelected(position: Int) {
                        LogUtils.i(TAG + "执行了2")
                        showImagePreview(mediaList[position], itemViewList.get(position))
                    }

                    override fun onPageScrollStateChanged(state: Int) {

                    }

                })
                vpPhoto.adapter = adapter
                LogUtils.i(TAG + "执行了adapter")
            }

        }

    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
//        initMediaManager1()
        LogUtils.i(TAG + "执行了position=$position")

    }

    private fun showImagePreview(mediaFile: MediaFile?, parentView: View) {
        if (mediaFile == null) {
            ToastUtil.showWarning("当前预览图为空")
            return
        }
        val task = FetchMediaTask(mediaFile, FetchMediaTaskContent.PREVIEW, FetchMediaTask.Callback { mediaFileOk, fetchMediaTaskContent, djiError ->
            if (djiError != null) {
                ToastUtil.showFailedDebug("照片获取失败" + djiError.description, "照片获取失败")
                return@Callback
            }
            previewMediaList.add(mediaFile)
            if (mediaFileOk.preview == null) {
                ToastUtil.showFailed("未获取到预览")
                return@Callback
            }
            val time = DateUtil.parseDateString("yyyy-MM-dd-HH:mm:ss", mediaFileOk.timeCreated)
            val imageView = parentView.findViewById<ImageView>(R.id.photoPreview)
            val ivPlayVideo = parentView.findViewById<ImageView>(R.id.ivPlayVideo)
            showTitle(time)
            val previewBitMap = mediaFile.preview
            runOnUiThread {
                GlideManager.loadImgAuto(previewBitMap, imageView)
                val isPhoto = mediaFileOk.mediaType == MediaFile.MediaType.JPEG || mediaFileOk.mediaType == MediaFile.MediaType.RAW_DNG
                setViewGone(ivPlayVideo, !isPhoto)
                if (!isPhoto) {
                    ivPlayVideo.setOnClickListener {
                        ToastUtil.showNormal("点击了")
                    }
                }
                mediaFile.resetPreview(null)
                mediaFile.resetThumbnail(null)
            }
        }

        )
        if (mediaManager != null) {
            mediaManager!!.scheduler.resume {
                if (it == null) {
                    mediaManager!!.scheduler.moveTaskToNext(task)
                    previewTaskList.add(task)
                } else {
                    ToastUtil.showNormalCondition("调度器启动失败" + it.description, "调度器启动失败")
                }
            }
        } else {
            ToastUtil.showNormal("当前无法访问相册")
        }

    }


    private fun getCamera(): Camera? {
        return if (!AircraftUtil.isCameraModuleAvailable()) {
            null
        } else ProductManager.getProductInstance().camera
    }

    private fun hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        val decorView: View = mActivity?.window!!.decorView
        decorView.systemUiVisibility = 0
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }


    private fun showTitle(title: String?) {
        runOnUiThread {
            tvPhotoTime?.text = StringUtil.getNotNullValueLine(title)
        }
    }



    private fun unsetMediaManager() {
        val camera = getCamera() ?: return
        if (mediaManager != null) {
            mediaManager!!.stop(null)
            if (stateListener != null) {
                mediaManager!!.removeFileListStateCallback(stateListener!!)
            }
            val iterator = previewMediaList.iterator()
            var mediaFile: MediaFile?
            while (iterator.hasNext()) {
                mediaFile = iterator.next()
                mediaFile = null
            }
            mediaManager!!.scheduler?.suspend(null)
            mediaManager!!.scheduler?.removeAllTasks()
            mediaManager!!.exitMediaDownloading()
            camera.exitPlayback(object : CommonCallbacks.CompletionCallback<DJIError?> {
                override fun onResult(djiError: DJIError?) {
                    LogUtils.w(AircraftPhotoFragment.TAG + djiError)
                }

            })
            val it = previewTaskList.iterator()
            var task: FetchMediaTask?
            while (it.hasNext()) {
                task = it.next()
                task = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsetMediaManager()
    }

    private fun findMediaPosition(list: List<MediaFile>?): Int {
        if (list == null || list.isEmpty() || mediaCreateTime == null || mediaCreateTime!! <= 0) {
            LogUtils.e(TAG + "mediaCreateTime=" + mediaCreateTime + "list.size=" + list!!.size)
            return -1
        }
        val size = list.size
        var mediaFile: MediaFile?
        for (i in 0 until size) {
            mediaFile = list[i]
            if (mediaFile.timeCreated == mediaCreateTime) {
                LogUtils.i("找到了媒体文件对应位置：$i")
                return i
            }
        }
        return -1
    }



    private fun showCurrentItem() {
        if (position == null || position!! < 0) {
            return
        }
        runOnUiThread {
            vpPhoto.setCurrentItem(position!!)
        }

    }

    private fun initMediaManager2() {
        if (ProductManager.getProductInstance() == null) {
            return
        }
        val camera = getCamera() ?: return
        if (!camera.isMediaDownloadModeSupported) {
            ToastUtil.showWarning("当前机型不支持下载模式")
            return
        }
        mediaManager = camera.mediaManager
    }

    private fun setViewGone(view: View?, visible: Boolean) {
        if (view == null) {
            return
        }
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}