package `in`.samlav.eartrainer

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder


const val MIN = 25
const val MAX = 500
const val STEP = 25

class CustomSeekBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = android.R.attr.preferenceStyle
) :
    Preference(context!!, attrs, defStyleAttr)
{
    var preferences: SharedPreferences
    private var value = 100

    override fun onBindViewHolder(holder: PreferenceViewHolder)
    {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false
        val seekBar = holder.findViewById(R.id.seekbar) as SeekBar
        seekBar.min = MIN
        seekBar.max = MAX
        val progress = holder.findViewById(R.id.progress) as TextView
        seekBar.progress = value
        progress.text = String.format("%.2f", (value / STEP) * 0.25)
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean)
            {
                val progressCustom = (p1 / STEP) * 0.25
                progress.text = String.format("%.2f", progressCustom)
                preferences.edit {
                    putInt("toneTime", (p1 / STEP) * 25)
                    commit()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?)
            {}

            override fun onStopTrackingTouch(p0: SeekBar?)
            {}
        })
    }

    init
    {
        widgetLayoutResource = R.layout.custom_seek_bar
        preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }!!
        value = if (preferences.contains("toneTime"))
        {
            preferences.getInt("toneTime", 100)
        }
        else
        {
            preferences.edit {
                putInt("toneTime", 100)
                commit()
            }
            100
        }
    }
}