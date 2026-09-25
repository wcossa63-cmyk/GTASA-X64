package com.blrp.game.gui.hud

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blrp.game.R
import com.blrp.game.core.Samp.Companion.activity
import com.blrp.game.databinding.HudBinding
import com.blrp.game.gui.util.ConvertViewCoordsToGta
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class HudManager : Chat(activity) {

    private val binding = HudBinding.bind(activity.findViewById(R.id.hudView))

    private val hud_money: TextView
    private val hud_main: ConstraintLayout
    private val hud_weapon: ImageView
    private val hud_ammo: TextView
    private val hud_bg: ImageView

    private val btn_menu: ConstraintLayout
    private val btn_action: ConstraintLayout
    private val btn_tablet: ConstraintLayout
    private val btn_donate: ConstraintLayout
    private val btn_inv: ConstraintLayout

    private var isHudSetPos = false
    private var weaponId: Int = 0
    private external fun HudInit()
    private external fun SetRadarBgPos(x1: Float, y1: Float, x2: Float, y2: Float)
    private external fun nativeSetRadarPos(x1: Float, y1: Float, width: Float, height: Float)
    private external fun changeWeapon()

    fun toggleProgressTexts(toggle: Boolean) {}

    fun updateBars(health: Int, armour: Int, hunger: Int) {
        binding.hpProgress.progress = health.coerceIn(0, 100) / 100f
        binding.armorProgress.progress = armour.coerceIn(0, 100) / 100f
    }

    fun updateAmmo(weaponid: Int, ammo: Int, ammoclip: Int) {
        activity.runOnUiThread {
            val id = activity.resources.getIdentifier("weapon_$weaponid", "drawable", activity.packageName)
            if (id > 0) {
                binding.weapon.setImageResource(id)
                weaponId = weaponid
            }

            if ((weaponid > 15) && (weaponid < 44) && (weaponid != 21)) {
                binding.weaponContainer.visibility = View.VISIBLE
                binding.weaponCartridges.text = ammoclip.toString()
                binding.weaponClip.text = "/${ammo - ammoclip}"
            } else {
                binding.weaponContainer.visibility = View.GONE
            }
        }
    }

    fun updateMoney(money: Int) {
        activity.runOnUiThread {
            val formatter = DecimalFormat()
            val symbols = DecimalFormatSymbols.getInstance()
            symbols.groupingSeparator = '.'
            formatter.decimalFormatSymbols = symbols
            binding.moneyCount.text = formatter.format(money.toLong())
        }
    }

    fun UpdateWanted(wantedLVL: Int) {
        activity.runOnUiThread {
            if (wantedLVL > 0) {
                binding.hudWanted.visibility = View.VISIBLE
                binding.hudWantedTxt.text = wantedLVL.toString()
                binding.hudWantedTxt.visibility = View.VISIBLE
            } else {
                binding.hudWanted.visibility = View.INVISIBLE
                binding.hudWantedTxt.visibility = View.INVISIBLE
            }
        }
    }

    fun toggleAll(toggle: Boolean, isWithChat: Boolean) {
        activity.runOnUiThread {
            hud_main.visibility = if (toggle) View.VISIBLE else View.GONE

            if (isWithChat) {
                chat_box.visibility = View.VISIBLE
                hide_chat.visibility = View.VISIBLE
            } else {
                chat_box.visibility = View.GONE
                hide_chat.visibility = View.GONE
            }
        }
    }

    fun toggleServerLogo(toggle: Boolean) {
        activity.runOnUiThread {
            val visibility = if (toggle) View.VISIBLE else View.INVISIBLE
            binding.serverLogo.visibility = visibility
            binding.serverNumber.visibility = visibility
            binding.serverNumberBg.visibility = visibility
        }
    }

    init {
        initServerLogo()
        UpdateWanted(0)
        HudInit()

        hud_bg = activity.findViewById(R.id.radar_zone)
        hud_main = activity.findViewById(R.id.hud_main)
        hud_money = activity.findViewById(R.id.money_count)
        hud_ammo = activity.findViewById(R.id.weapon_cartridges)
        hud_weapon = activity.findViewById(R.id.weapon)

        btn_menu = activity.findViewById(R.id.menu_btn)
        btn_menu.setOnClickListener {
            btn_menu.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(60)
                .withEndAction {
                    btn_menu.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }.start()
        }
        btn_action = activity.findViewById(R.id.quest_btn)
        btn_action.setOnClickListener {
            btn_action.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(60)
                .withEndAction {
                    btn_action.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }.start()
        }
        btn_tablet = activity.findViewById(R.id.tablet_btn)
        btn_tablet.setOnClickListener {
            btn_tablet.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(60)
                .withEndAction {
                    btn_tablet.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }.start()
        }
        btn_donate = activity.findViewById(R.id.shop_btn)
        btn_donate.setOnClickListener {
            btn_donate.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(60)
                .withEndAction {
                    btn_donate.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }.start()
        }
        btn_inv = activity.findViewById(R.id.inventory_btn)
        btn_inv.setOnClickListener {
            btn_inv.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(60)
                .withEndAction {
                    btn_inv.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }.start()
        }
        hud_weapon.setOnClickListener { changeWeapon() }
        hud_bg.post {
            if (isHudSetPos) return@post
            SetRadarBgPos(hud_bg.x, hud_bg.y, hud_bg.x + hud_bg.width, hud_bg.y + hud_bg.height)

            val data = ConvertViewCoordsToGta.convertCoordsToGta(
                ConvertViewCoordsToGta.Data(
                    binding.radarZone.x,
                    binding.radarZone.y,
                    binding.radarZone.width.toFloat(),
                    binding.radarZone.height.toFloat()
                )
            )
            nativeSetRadarPos(data.x, data.y, data.width * 0.96f, data.width * 0.96f)

            activity.runOnUiThread {
                hud_main.visibility = View.GONE
                hud_bg.visibility = View.GONE
            }
            isHudSetPos = true
        }
    }

    private fun initServerLogo() {
//
    }
}