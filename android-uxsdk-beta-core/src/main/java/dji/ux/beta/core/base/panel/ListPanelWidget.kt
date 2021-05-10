/*
 * Copyright (c) 2018-2020 DJI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dji.ux.beta.core.base.panel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import dji.ux.beta.core.base.SchedulerProvider
import dji.ux.beta.core.util.ViewIDGenerator

/**

 *[ListPanelWidget]的基类。[ListPanelWidget]是小部件的垂直集合。

 *[ListPanelWidget]的子窗口小部件与[BarPanelWidget]的子窗口小部件不同，

 *是动态创建的，列表可以根据MSDK级别的更改而更改。

 *

 *这个列表并不打算用作无限列表，而是用于有限数量的小部件。

 *对于无限列表，最好使用RecyclerView。

 *

 *此列表中的小部件具有以下属性：

 *-宽度：匹配父项

 *-高度：包装内容

 *

 *ListPanelWidget可能包含一个默认的[SmartListModel]，它将视图注入到这个列表中

 *ListPanelWidget。也可以通过设置[SmartListModel]来覆盖[SmartListModel]，允许

 *用户更改列表的行为。

 *

 *定制：

 *[ListPanelWidget]在内部使用ListView。要自定义分隔符，用户可以使用

 *ListView的属性：

 * android:divider=“#FFCCFF”

 * android:dividerHeight=“4dp”

 *用户还可以移除分隔器：

 * android:divider=“@null”

 */
abstract class ListPanelWidget<T> @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        configuration: PanelWidgetConfiguration
) : PanelWidget<View, T>(context, attrs, defStyleAttr, configuration) {

    //region Properties
    /**
     * Optional [SmartListModel].
     * Setting a new instance refreshes the whole list.
     */
    var smartListModel: SmartListModel? = null
        set(value) {
            field = value
            field?.setListPanelWidgetHolder(listPanelWidgetBaseModel)
            if (ViewCompat.isAttachedToWindow(this)) {
                field?.setUp()
            }
            if (field != null) {
                onSmartListModelCreated()
            }
        }

    /**
     * Default [ListPanelWidgetBaseModel], can be overwritten.
     */
    protected open val listPanelWidgetBaseModel: ListPanelWidgetBaseModel = ListPanelWidgetBaseModel()
    private val adapter = Adapter()
    //endregion

    //region Constructor
    init {
        check(panelWidgetConfiguration.panelWidgetType == PanelWidgetType.LIST) {
            "PanelWidgetConfiguration.panelWidgetType should be PanelWidgetType.LIST"
        }

        setUpListView(attrs)
        // Set padding on the parent to 0, so only the listview can change padding
        setPadding(0, 0, 0, 0)
    }

    private fun setUpListView(attrs: AttributeSet?) {
        val listView = ListView(context, attrs)
        listView.id = ViewIDGenerator.generateViewId()
        listView.adapter = adapter
        addView(listView)
        listView.visibility = View.VISIBLE

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        constraintSet.constrainWidth(listView.id, 0)
        constraintSet.constrainHeight(listView.id, 0)
        constraintSet.constraintToParentStart(listView)
        constraintSet.constraintToParentEnd(listView)
        constraintSet.constraintToParentBottom(listView)
        constraintSet.constraintToParentTop(listView)

        constraintSet.applyTo(this)
    }
    //endregion

    //region Lifecycle
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            smartListModel?.setUp()

            listPanelWidgetBaseModel.widgetList
                    .observeOn(SchedulerProvider.ui())
                    .subscribe { updateUI() }
        }
    }

    override fun onDetachedFromWindow() {
        smartListModel?.cleanUp()
        super.onDetachedFromWindow()
    }


    /**
     * Call to refresh the list.
     */
    override fun updateUI() {
        adapter.notifyDataSetChanged()
    }

    /**
     * Callback for when a new [SmartListModel] is created.
     */
    protected abstract fun onSmartListModelCreated()
    //endregion

    //region Populate Panel
    /**
     * Get the [View] at index from the current list of widgets.
     */
    override fun getWidget(index: Int): View? {
        smartListModel?.let { return it.getActiveWidget(index) }
        return listPanelWidgetBaseModel.getWidget(index)
    }

    /**
     * Add a new [List] of [View].
     */
    override fun addWidgets(items: Array<View>) {
        if (smartListModel == null) {
            listPanelWidgetBaseModel.addWidgets(items.toList())
        }
    }

    /**
     * Total size of [View] in the current list of widgets.
     */
    override fun size(): Int {
        smartListModel?.let { return it.activeWidgetSize }
        return listPanelWidgetBaseModel.size()
    }

    /**
     * Add a [View] at [index] to the current list of widgets.
     */
    override fun addWidget(index: Int, view: View) {
        if (smartListModel == null) {
            listPanelWidgetBaseModel.addWidget(index, view)
        }
    }

    /**
     * Remove a [View] at [index] from the current list of widgets.
     */
    override fun removeWidget(index: Int): View? {
        if (smartListModel == null) {
            return listPanelWidgetBaseModel.removeWidget(index)
        }
        return null
    }

    /**
     * Remove all [View]s.
     */
    override fun removeAllWidgets() {
        if (smartListModel == null) {
            listPanelWidgetBaseModel.removeAllWidgets()
        }
    }
    //endregion

    //region Customization
    override fun getIdealDimensionRatioString(): String? = null
    //endregion

    private inner class Adapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = listPanelWidgetBaseModel.getWidget(position)
                    ?: throw IllegalAccessException("View not found at position $position")
            if (view is Navigable) {
                view.panelNavigator = this@ListPanelWidget.panelNavigator
            }
            view.setHasTransientState(true)
            return view
        }

        override fun getItem(position: Int): Any {
            return listPanelWidgetBaseModel.getWidget(position)
                    ?: throw IllegalAccessException("Item not found at position $position")
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = listPanelWidgetBaseModel.size()

    }
}