package com.blrp.game.gui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.blrp.game.EasyAnimation.setOnClickListenerWithAnim
import com.blrp.game.core.Samp.Companion.activity
import com.blrp.game.databinding.HudSpeedometrBinding

class Speedometer : NativeGui<HudSpeedometrBinding>(HudSpeedometrBinding::class) {
    override fun receivePacket(type: Int, data: String) {
        TODO("Not yet implemented")
    }

    private var currentHPProgress = 0
    private var currentLight = -1
    private var currentEngine = -1
    private var currentLock = -1
    private val BUTTON_TURN_LEFT: Int = 2
    private val BUTTON_TURN_RIGHT: Int = 3
    private val BUTTON_TURN_ALL: Int = 4
    private val BUTTON_ENGINE: Int = 0
    private val BUTTON_LIGHT: Int = 1
    private val BUTTON_DOOR: Int = 5

    enum class eTurnLightState {
        TURNLIGHT_OFF,
        TURNLIGHT_LEFT,
        TURNLIGHT_RIGHT,
        TURNLIGHT_ALL;
    }

    private var maxHp = 1000
    private var maxTankLevel = 100
    private external fun nativeGetMaxSpeed(): Int
    external fun sendClick(clickId: Int)

    init {
        activity.runOnUiThread {
            if (SpeedometerStatic.currentFuelProgress != -1) {
                binding.fuelText.text = SpeedometerStatic.currentFuelProgress.toString() + " Л"
                binding.fuelProgress.progress = SpeedometerStatic.currentFuelProgress
            }
            binding.leftTurnLight.setOnClickListenerWithAnim {
                sendClick(BUTTON_TURN_LEFT)
            }
            binding.rightTurnLight.setOnClickListenerWithAnim {
                sendClick(BUTTON_TURN_RIGHT)
            }
            binding.emergency.setOnClickListenerWithAnim {
                sendClick(BUTTON_TURN_ALL)
            }
            binding.lights.setOnClickListenerWithAnim {
                sendClick(BUTTON_LIGHT)
            }
            binding.speedLayout.setOnClickListenerWithAnim {
                sendClick(BUTTON_ENGINE)
            }
            binding.speed.setMaxSpeed(nativeGetMaxSpeed())
        }
    }

    private fun updateSpeed(speed: Int) {
        activity.runOnUiThread {
            binding.speed.setCurrentSpeed(speed)
        }
    }

    private fun updateInfo(fuel: Int, hp: Int, mileage: Int, engine: Int, light: Int, lock: Int, turnlight: Int) {
        activity.runOnUiThread {
            val health: Int = ((hp.toFloat() * 100.0f) / maxHp).toInt()

            if (this.currentHPProgress != hp) {
                this.currentHPProgress = hp
                binding.healthProgress.setMax(maxHp)
                binding.healthProgress.progress = hp
                binding.healthText.text = "${health}%"
            }
            if (light == 0) {
                binding.lights.setImageTintList(null)
                binding.lightsIcon.setImageTintList(null)
            } else {
                binding.lights.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FBEF6F")))
                binding.lightsIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FBEF6F")))
            }
            if (engine == 0) {
                binding.engineIcon.setImageTintList(null)
            } else {
                binding.engineIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#B9FF55")))
            }
            if (lock == 0) {
                binding.doorIcon.setImageTintList(null)
            } else {
                binding.doorIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FBDB6F")))
            }
        }
    }

    private fun updateFuel(fuel: Int) {
        activity.runOnUiThread {
            if (SpeedometerStatic.currentFuelProgress != fuel) {
                SpeedometerStatic.currentFuelProgress = fuel
                binding.fuelText.text = "$fuel Л"
                binding.fuelProgress.progress = fuel
            }
        }
    }

    private fun setMaxCarValues(maxHp: Int, maxTank: Int) {
        activity.runOnUiThread {
            this.maxHp = maxHp
            this.maxTankLevel = maxTank
            binding.fuelProgress.setMax(maxTank)
            binding.healthProgress.setMax(maxHp)
        }
    }

    fun tempToggle(toggle: Boolean) {
        if (toggle) activity.runOnUiThread { binding.speedometerMainLayout.visibility = View.VISIBLE }
        else activity.runOnUiThread { binding.speedometerMainLayout.visibility = View.GONE }
    }

    fun updateTurn(isTurn: Boolean, turnLightState: Int) {
        activity.runOnUiThread {
            if (!isTurn || turnLightState == eTurnLightState.TURNLIGHT_OFF.ordinal) {
                binding.emergency.setImageTintList(null)
                binding.rightTurnLight.setImageTintList(null)
                binding.leftTurnLight.setImageTintList(null)
            } else if (turnLightState == eTurnLightState.TURNLIGHT_LEFT.ordinal) {
                binding.leftTurnLight.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FBDB6F")))
            } else if (turnLightState == eTurnLightState.TURNLIGHT_RIGHT.ordinal) {
                binding.rightTurnLight.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FBDB6F")))
            } else if (turnLightState == eTurnLightState.TURNLIGHT_ALL.ordinal) {
                binding.emergency.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF4646")))
            }
        }
    }
}