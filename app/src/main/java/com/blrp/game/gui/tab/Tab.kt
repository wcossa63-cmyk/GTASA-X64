package com.blrp.game.gui.tab

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.blrp.game.core.Samp.Companion.activity
import com.blrp.game.databinding.TabBinding
import com.blrp.game.gui.NativeGui
import com.blrp.game.gui.util.LinearLayoutManagerWrapper

class Tab : NativeGui<TabBinding>(TabBinding::class) {

    private external fun nativeDeleteCppObj()

    private val mTabAdapter: TabAdapter = TabAdapter()

    init {
        activity.runOnUiThread {

            binding.searchTextEdit.setOnClickListener {
                clearEditText()
            }

            binding.usersRc.layoutManager = LinearLayoutManagerWrapper(activity)
            binding.usersRc.adapter = mTabAdapter

            binding.searchTextEdit.setText("")
            setVisibleIconInSearchView("")

            binding.exitButton.setOnClickListener {
                destroy()
            }

            binding.searchTextEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    mTabAdapter.updateSearch(editable.toString())
                    setVisibleIconInSearchView(editable.toString())
                }
            })
        }
    }

    private fun clearEditText() {
        binding.searchTextEdit.setText("")
        setVisibleIconInSearchView("")
    }

    override fun destroy() {
        super.destroy()

        activity.runOnUiThread {
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.searchTextEdit.windowToken, 0)
            nativeDeleteCppObj()
        }
    }

    override fun receivePacket(actionId: Int, data: String) {
        TODO("Not yet implemented")
    }

    fun setStat(id: Int, color: Int, name: String?, score: Int, ping: Int) {
        activity.runOnUiThread {
            mTabAdapter.addItem(PlayerData(id, color, name, score, ping))
        }
    }

    fun setVisibleIconInSearchView(str: String) {
        activity.runOnUiThread {
            if (str.isEmpty()) {
                binding.searchTextEdit.visibility = View.VISIBLE
                return@runOnUiThread
            }
        }
    }
}