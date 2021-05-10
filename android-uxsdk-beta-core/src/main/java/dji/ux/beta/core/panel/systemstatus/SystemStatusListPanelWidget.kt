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

package dji.ux.beta.core.panel.systemstatus

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.use
import dji.ux.beta.core.R
import dji.ux.beta.core.base.panel.ListPanelWidget
import dji.ux.beta.core.base.panel.PanelWidgetConfiguration
import dji.ux.beta.core.base.panel.PanelWidgetType
import dji.ux.beta.core.base.panel.WidgetID
import dji.ux.beta.core.communication.OnStateChangeCallback
import dji.ux.beta.core.extension.getIntegerAndUse
import dji.ux.beta.core.extension.toggleVisibility
import dji.ux.beta.core.panel.listitem.aircraftbatterytemperature.AircraftBatteryTemperatureListItemWidget
import dji.ux.beta.core.panel.listitem.emmcstatus.EMMCStatusListItemWidget
import dji.ux.beta.core.panel.listitem.flightmode.FlightModeListItemWidget
import dji.ux.beta.core.panel.listitem.maxaltitude.MaxAltitudeListItemWidget
import dji.ux.beta.core.panel.listitem.maxflightdistance.MaxFlightDistanceListItemWidget
import dji.ux.beta.core.panel.listitem.overview.OverviewListItemWidget
import dji.ux.beta.core.panel.listitem.rcbattery.RCBatteryListItemWidget
import dji.ux.beta.core.panel.listitem.rcstickmode.RCStickModeListItemWidget
import dji.ux.beta.core.panel.listitem.returntohomealtitude.ReturnToHomeAltitudeListItemWidget
import dji.ux.beta.core.panel.listitem.sdcardstatus.SDCardStatusListItemWidget
import dji.ux.beta.core.panel.listitem.travelmode.TravelModeListItemWidget
import dji.ux.beta.core.widget.systemstatus.SystemStatusWidget

/**

 *要允许用户切换隐藏和显示此面板小部件，请结合使用

 *使用[SystemStatusWidget]。

 *

 *此面板小部件显示系统状态列表，其中包括项目列表（如IMU、GPS等）。

 *这个面板小部件的当前版本是一个示例，还有更多的项目要提供

 *在以后的版本中。

 *

 *定制：

 *使用“excludeItem”属性从列表中永久删除项。这将防止

 *在面板小部件的整个生命周期中，某些项目不会被创建和显示。给你

 *所有标志：飞行模式，指南针，视觉传感器，无线电质量，遥控棒模式，遥控电池，

 *飞机电池温度，sd卡状态，emmc状态，最大高度，最大飞行距离，

 *旅行模式。

 *

 *注意，通过逻辑“或”可以同时使用多个标志

 *他们。例如，要隐藏sd卡状态和rc stick模式，可以通过

 *以下两个步骤。

 *在其布局文件中定义自定义xmlns：

 * xmlns:app="http://schemas.android.com/apk/res-auto"

 *然后，向SystemStatusListPanelWidget添加以下属性：

 * app:excludeItem=“sd卡|状态| rc |粘贴|模式”。

 *

 *这个面板小部件还将属性传递给创建的每个子小部件。看到每个

 *用于单独定制的小部件：

 *[概述ListItemWidget]，

 *[返回HomeAltitudeListItemWidget]，

 *[FlightModeListItemWidget]，

 *[RCStickModelListItemWidget]，

 *[RCBatteryListItemWidget]，

 *[AircraftBatteryTemperatureListItemWidget]，

 *[SDCardStatusListItemWidget]，

 *[EMMCStatusListItemWidget]，

 *[MaxAltitudeListItemWidget]，

 *[MaxFlightDistanceListItemWidget]，

 *[TravelModeListItemWidget]。

 *

 *要自定义各个小部件，请以XML形式传递主题：

 *<代码>android:theme=“@style/uxsdksystemstatuslistsetheme”</code

 */
class SystemStatusListPanelWidget @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        configuration: PanelWidgetConfiguration = PanelWidgetConfiguration(
                context,
                PanelWidgetType.LIST,
                showTitleBar = true,
                panelTitle = context.getString(R.string.uxsdk_system_status_list_title),
                hasCloseButton = true)
) : ListPanelWidget<Any>(context, attrs, defStyleAttr, configuration), OnStateChangeCallback<Any?> {

    //region Lifecycle
    override fun initPanelWidget(context: Context, attrs: AttributeSet?, defStyleAttr: Int, widgetConfiguration: PanelWidgetConfiguration?) {
        // Nothing to do
    }

    init {
        val excludedItemsValue = attrs?.let { initAttributes(context, it) }
        val excludedItemsSet = getExcludedItems(excludedItemsValue)

        smartListModel = SystemStatusSmartListModel(context, attrs, excludedItemsSet)
    }

    override fun onSmartListModelCreated() {
        // Nothing to do
    }


    override fun reactToModelChanges() {
        // Nothing to do
    }

    override fun onStateChange(state: Any?) {
        toggleVisibility()
    }
    //endregion

    //region Customizations
    @SuppressLint("Recycle")
    private fun initAttributes(context: Context, attrs: AttributeSet): Int {
        context.obtainStyledAttributes(attrs, R.styleable.SystemStatusListPanelWidget).use { typedArray ->
            typedArray.getIntegerAndUse(R.styleable.SystemStatusListPanelWidget_uxsdk_excludeItem) {
                return it
            }
        }

        return 0
    }
    //endregion

    //region Helpers
    private fun getExcludedItems(excludedItemsValue: Int?): Set<WidgetID>? {
        return if (excludedItemsValue != null) {
            SystemStatusSmartListModel.SystemStatusListItem.values
                    .filter { it.isItemExcluded(excludedItemsValue) }
                    .map { it.widgetID }
                    .toSet()
        } else {
            null
        }
    }
    //endregion

}