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

package dji.ux.beta.flight.widget.takeoff

import dji.common.flightcontroller.VisionLandingProtectionState
import dji.common.product.Model
import dji.common.remotecontroller.RCMode
import dji.keysdk.*
import dji.thirdparty.io.reactivex.Completable
import dji.thirdparty.io.reactivex.Flowable
import dji.thirdparty.io.reactivex.Single
import dji.ux.beta.core.base.DJISDKModel
import dji.ux.beta.core.base.WidgetModel
import dji.ux.beta.core.communication.GlobalPreferenceKeys
import dji.ux.beta.core.communication.GlobalPreferencesInterface
import dji.ux.beta.core.communication.ObservableInMemoryKeyedStore
import dji.ux.beta.core.communication.UXKeys
import dji.ux.beta.core.util.DataProcessor
import dji.ux.beta.core.util.ProductUtil
import dji.ux.beta.core.util.UnitConversionUtil

private const val TAKEOFF_HEIGHT: Float = 1.2f
private const val PRECISION_TAKEOFF_HEIGHT: Float = 6f
private const val LAND_HEIGHT: Float = 0.3f

/**
 * Widget Model for the [TakeOffWidget] used to define
 * the underlying logic and communication
 */
class TakeOffWidgetModel(djiSdkModel: DJISDKModel,
                         keyedStore: ObservableInMemoryKeyedStore,
                         private val preferencesManager: GlobalPreferencesInterface?
) : WidgetModel(djiSdkModel, keyedStore) {
    private var aircraftAltitudeKey: DJIKey? = null

    //region Fields
    private val isFlyingDataProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val isAutoLandingDataProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val isLandingConfNeededDataProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val forceLandingHeightDataProcessor: DataProcessor<Int> = DataProcessor.create(0)
    private val areMotorsOnDataProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val isGoingHomeDataProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val flightModeStringDataProcessor: DataProcessor<String> = DataProcessor.create("")
    private val isCancelAutoLandingDisabledProcessor: DataProcessor<Boolean> = DataProcessor.create(false)
    private val rcModeDataProcessor: DataProcessor<RCMode> = DataProcessor.create(RCMode.UNKNOWN)
    private val productModelProcessor: DataProcessor<Model> = DataProcessor.create(Model.UNKNOWN_AIRCRAFT)
    private val unitTypeProcessor: DataProcessor<UnitConversionUtil.UnitType> = DataProcessor.create(UnitConversionUtil.UnitType.METRIC)
    private val landingProtectionStateDataProcessor: DataProcessor<VisionLandingProtectionState> = DataProcessor.create(VisionLandingProtectionState.UNKNOWN)
    private val takeOffLandingStateDataProcessor: DataProcessor<TakeOffLandingState> =
            DataProcessor.create(TakeOffLandingState.DISCONNECTED)
    private val isInAttiModeDataProcessor: DataProcessor<Boolean> =
            DataProcessor.create(false)

    private var takeOffListener: TakeOffListener? = null

    //是否起飞
    private var isFlying = false

    //endregion

    //region Data
    /**
     * Get the takeoff landing state
     */
    val takeOffLandingState: Flowable<TakeOffLandingState>
        get() = takeOffLandingStateDataProcessor.toFlowable().distinctUntilChanged()

    /**
     * Get whether precision takeoff is supported
     */
    val isPrecisionTakeoffSupported: Single<Boolean>
        get() = djiSdkModel.getValue(FlightControllerKey.create(FlightControllerKey.IS_PRECISION_TAKE_OFF_SUPPORTED))
                .map { it as Boolean && djiSdkModel.isKeySupported(FlightControllerKey.create(FlightControllerKey.PRECISION_TAKE_OFF)) }

    /**
     * Get whether the product is in ATTI mode
     */
    val isInAttiMode: Flowable<Boolean>
        get() = isInAttiModeDataProcessor.toFlowable()

    /**
     * Get whether the product is an Inspire 2 or part of the Matrice 200 series
     */
    val isInspire2OrMatrice200Series: Flowable<Boolean>
        get() = productModelProcessor.toFlowable().map { model: Model? ->
            model == Model.INSPIRE_2 || ProductUtil.isMatrice200Series(model)
        }

    /**
     * Get the height the aircraft will reach after takeoff
     */
    val takeOffHeight: Height
        get() = getHeightFromValue(TAKEOFF_HEIGHT)

    /**
     * Get the height the aircraft will reach after a precision takeoff
     */
    val precisionTakeOffHeight: Height
        get() = getHeightFromValue(PRECISION_TAKEOFF_HEIGHT)

    /**
     * Get the current height of the aircraft while waiting for landing confirmation
     *
     * 在等待着陆确认时获取飞机的当前高度
     */
    val landHeight: Height
        get() = getHeightFromValue(getLandHeight())
    //endregion

    //region Constructor
    init {
        if (preferencesManager != null) {
            unitTypeProcessor.onNext(preferencesManager.unitType)
        }
    }
    //endregion

    //region Actions
    /**
     * Performs take off action
     * 执行起飞动作
     */
    fun performTakeOffAction(): Completable {
        val takeoff: DJIKey = FlightControllerKey.create(FlightControllerKey.TAKE_OFF)
        return djiSdkModel.performAction(takeoff)
                .onErrorResumeNext { error: Throwable? ->
                    if (areMotorsOnDataProcessor.value) {
                        return@onErrorResumeNext Completable.complete()
                    } else {
                        return@onErrorResumeNext Completable.error(error)
                    }
                }
    }

    /**
     * Performs precision take off action
     */
    fun performPrecisionTakeOffAction(): Completable {
        val takeoff: DJIKey = FlightControllerKey.create(FlightControllerKey.PRECISION_TAKE_OFF)
        return djiSdkModel.performAction(takeoff)
                .onErrorResumeNext { error: Throwable? ->
                    if (areMotorsOnDataProcessor.value) {
                        return@onErrorResumeNext Completable.complete()
                    } else {
                        return@onErrorResumeNext Completable.error(error)
                    }
                }
    }

    /**
     * Performs landing action
     */
    fun performLandingAction(): Completable {
        val landAction: DJIKey = FlightControllerKey.create(FlightControllerKey.START_LANDING)
        return djiSdkModel.performAction(landAction)
    }

    /**
     * Performs cancel landing action
     */
    fun performCancelLandingAction(): Completable {
        val cancelLanding: DJIKey = FlightControllerKey.create(FlightControllerKey.CANCEL_LANDING)
        return djiSdkModel.performAction(cancelLanding)
    }

    /**
     * Performs the landing confirmation action. This allows aircraft to land when
     * landing confirmation is received.
     * 执行着陆确认动作。这使得飞机在

     *收到着陆确认。
     */
    fun performLandingConfirmationAction(): Completable {
        val forceAction: DJIKey = FlightControllerKey.create(FlightControllerKey.CONFIRM_LANDING)
        return djiSdkModel.performAction(forceAction)
    }

    //endregion

    //region Lifecycle
    override fun inSetup() {
        val isFlyingKey: DJIKey = FlightControllerKey.create(FlightControllerKey.IS_FLYING)
        bindDataProcessor(isFlyingKey, isFlyingDataProcessor)
        val isAutoLandingKey: DJIKey = FlightControllerKey.create(FlightControllerKey.IS_LANDING)
        bindDataProcessor(isAutoLandingKey, isAutoLandingDataProcessor)
        val isLandingConfNeededKey: DJIKey = FlightControllerKey.create(FlightControllerKey.IS_LANDING_CONFIRMATION_NEEDED)
        bindDataProcessor(isLandingConfNeededKey, isLandingConfNeededDataProcessor)
        val forceLandingHeightKey: DJIKey = FlightControllerKey.create(FlightControllerKey.FORCE_LANDING_HEIGHT)
        bindDataProcessor(forceLandingHeightKey, forceLandingHeightDataProcessor)
        val flightModeStringKey: DJIKey = FlightControllerKey.create(FlightControllerKey.FLIGHT_MODE_STRING)
        bindDataProcessor(flightModeStringKey, flightModeStringDataProcessor) { value: Any ->
            isInAttiModeDataProcessor.onNext((value as String).contains("atti", ignoreCase = true))
        }
        val areMotorsOnKey: DJIKey = FlightControllerKey.create(FlightControllerKey.ARE_MOTOR_ON)
        bindDataProcessor(areMotorsOnKey, areMotorsOnDataProcessor)
        val isGoingHomeKey: DJIKey = FlightControllerKey.create(FlightControllerKey.IS_GOING_HOME)
        bindDataProcessor(isGoingHomeKey, isGoingHomeDataProcessor)
        val isCancelAutoLandingDisabledKey: DJIKey = FlightControllerKey.create(FlightControllerKey.IS_CANCEL_AUTO_LANDING_DISABLED)
        bindDataProcessor(isCancelAutoLandingDisabledKey, isCancelAutoLandingDisabledProcessor)
        val rcModeKey: DJIKey = RemoteControllerKey.create(RemoteControllerKey.MODE)
        bindDataProcessor(rcModeKey, rcModeDataProcessor)
        val productModelKey: DJIKey = ProductKey.create(ProductKey.MODEL_NAME)
        bindDataProcessor(productModelKey, productModelProcessor)
        val unitKey = UXKeys.create(GlobalPreferenceKeys.UNIT_TYPE)
        bindDataProcessor(unitKey, unitTypeProcessor)
        val landingProtectionStateKey: DJIKey = FlightControllerKey.createFlightAssistantKey(FlightControllerKey.LANDING_PROTECTION_STATE)
        bindDataProcessor(landingProtectionStateKey, landingProtectionStateDataProcessor)
        preferencesManager?.setUpListener()
    }

    override fun inCleanup() {
        preferencesManager?.cleanup()
    }

    override fun updateStates() {
        if (!productConnectionProcessor.value) {
            takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.DISCONNECTED)
        } else if (isAutoLandingDataProcessor.value) {
            if (isLandingConfNeededDataProcessor.value) {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.WAITING_FOR_LANDING_CONFIRMATION)
            } else if (isCancelAutoLandingDisabled()) {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.FORCED_AUTO_LANDING)
            } else if (landingProtectionStateDataProcessor.value == VisionLandingProtectionState.NOT_SAFE_TO_LAND) {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.UNSAFE_TO_LAND)
            } else {
                //自动着陆状态
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.AUTO_LANDING)
            }
        } else if (isGoingHomeDataProcessor.value && !isAutoLandingDataProcessor.value) {
            takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.RETURNING_TO_HOME)
        } else if (!areMotorsOnDataProcessor.value) {
            if (rcModeDataProcessor.value == RCMode.SLAVE) {
                //如果电机还是在转 则说明
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.TAKE_OFF_DISABLED)
            } else {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.READY_TO_TAKE_OFF)
                if (takeOffListener != null && isFlying) {
                    takeOffListener!!.onLandSuccess()
                    isFlying = false
                }
            }
        } else {
            if (rcModeDataProcessor.value == RCMode.SLAVE) {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.LAND_DISABLED)
            } else {
                takeOffLandingStateDataProcessor.onNext(TakeOffLandingState.READY_TO_LAND)
            }
        }
    }

    private fun isCancelAutoLandingDisabled(): Boolean {
        return isCancelAutoLandingDisabledProcessor.value ||
                rcModeDataProcessor.value == RCMode.SLAVE
    }

    private fun getHeightFromValue(value: Float): Height {
        return Height(
                if (unitTypeProcessor.value == UnitConversionUtil.UnitType.IMPERIAL) {
                    UnitConversionUtil.convertMetersToFeet(value)
                } else {
                    value
                },
                unitTypeProcessor.value)
    }

    private fun getLandHeight(): Float {
        return if (forceLandingHeightDataProcessor.value != Int.MIN_VALUE) {
            forceLandingHeightDataProcessor.value * 0.1f
        } else {
            LAND_HEIGHT
        }
    }
    //endregion

    //region Classes
    /**
     * The state of the aircraft
     */
    enum class TakeOffLandingState {
        /**
         * The aircraft is ready to take off
         * 飞机准备起飞
         */
        READY_TO_TAKE_OFF,

        /**
         * The aircraft is currently flying and is ready to land
         * 飞机目前正在飞行，准备降落
         */
        READY_TO_LAND,

        /**
         * The aircraft has started auto landing
         * 飞机已开始自动着陆
         */
        AUTO_LANDING,

        /**
         * The aircraft has started auto landing and it cannot be canceled
         */
        FORCED_AUTO_LANDING,

        /**
         * The aircraft has paused auto landing and is waiting for confirmation before continuing
         */
        WAITING_FOR_LANDING_CONFIRMATION,

        /**
         * The aircraft has determined it is unsafe to land while auto landing is in progress
         *
         * 飞机已确定着陆时自动着陆是不安全的。
         */
        UNSAFE_TO_LAND,

        /**
         * The aircraft is returning to its home point
         */
        RETURNING_TO_HOME,

        /**
         * The aircraft cannot take off
         */
        TAKE_OFF_DISABLED,

        /**
         * The aircraft cannot land
         */
        LAND_DISABLED,

        /**
         * The aircraft is disconnected
         */
        DISCONNECTED
    }

    /**
     * Represents a height and the height's unit.
     *
     * @property height The current height of the aircraft in [unitType]
     * @property unitType The unit type of [height]
     */
    data class Height(val height: Float,
                      val unitType: UnitConversionUtil.UnitType)
    //endregion


    /**
     * 获取当前高度
     */
    private fun getCurrentHeight(): Float? {
        if (KeyManager.getInstance() == null) {
            return null
        }
        if (aircraftAltitudeKey == null) {
            return null
        }
        val heightValue = KeyManager.getInstance().getValue(aircraftAltitudeKey!!) ?: return null
        return transformValue(heightValue, aircraftAltitudeKey!!)
    }


    private fun transformValue(value: Any?, key: DJIKey): Float? {
        return if (key == aircraftAltitudeKey) {
            if (value != null) {
                value as Float?
            } else null
        } else null
    }


    fun release() {
        if (aircraftAltitudeKey != null) {
            KeyManager.getInstance().removeKey(aircraftAltitudeKey)
        }

    }

    fun setTakeOffListener(listener: TakeOffListener?) {
        this.takeOffListener = listener
    }

    fun setIsFlying(value: Boolean) {
        this.isFlying = value
    }

}