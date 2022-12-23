package `in`.samlav.eartrainer

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceManager

/**
 * Settings fragment
 *
 * @constructor Create empty Settings fragment
 */
class SettingsFragment : PreferenceFragmentCompat()
{
    lateinit var numBins: ListPreference
    lateinit var whichBins: MultiSelectListPreference
    lateinit var immediateFeedback: SwitchPreference
    lateinit var numTries: SeekBarPreference
    lateinit var preferences: SharedPreferences
    lateinit var useEQ: SwitchPreference
    lateinit var highEndPad: SeekBarPreference
    lateinit var crossoverFreq: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }!!
        numBins = findPreference("numBins")!!
        whichBins = findPreference("whichBins")!!
        immediateFeedback = findPreference("immediateFeedback")!!
        numTries = findPreference("numTries")!!
        useEQ = findPreference("useEQ")!!
        highEndPad = findPreference("highEndPad")!!
        crossoverFreq = findPreference("crossoverFreq")!!

        numBins.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            changeBins(newValue as String)
            return@OnPreferenceChangeListener true
        }
        useEQ.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            onUseEQChange(newValue as Boolean)
            return@OnPreferenceChangeListener true
        }
        immediateFeedback.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue == true)
            {
                preferences.edit {
                    putInt("numTries", numTries.value)
                    commit()
                }
                numTries.isEnabled = true
            }
            else
            {
                preferences.edit {
                    putInt("numTries", 1)
                    commit()
                }
                numTries.isEnabled = false
            }
            return@OnPreferenceChangeListener true
        }
        numTries.isEnabled = immediateFeedback.isChecked
        changeBins(numBins.value)
        onUseEQChange(useEQ.isChecked)
    }

    /**
     * Helper method called every time the number of bins is changed; changes which entries array is used for the [whichBins] [Preference]
     *
     * @param newValue
     */
    private fun changeBins(newValue: String)
    {
        when (newValue)
        {
            "10" ->
            {
                whichBins.entries = resources.getStringArray(R.array.ten_bin_entries)
                whichBins.entryValues = resources.getStringArray(R.array.ten_bin_entries)
            }
            "15" ->
            {
                whichBins.entries = resources.getStringArray(R.array.fifteen_bin_entries)
                whichBins.entryValues = resources.getStringArray(R.array.fifteen_bin_entries)
            }
            "20" ->
            {
                whichBins.entries = resources.getStringArray(R.array.twenty_bin_entries)
                whichBins.entryValues = resources.getStringArray(R.array.twenty_bin_entries)
            }
            "31" ->
            {
                whichBins.entries = resources.getStringArray(R.array.thirty_one_bin_entries)
                whichBins.entryValues = resources.getStringArray(R.array.thirty_one_bin_entries)
            }
        }
    }

    /**
     * Helper method called whenever the user enables/disables the EQ and enables/disables the proper [Preference]s
     *
     * @param newValue
     */
    private fun onUseEQChange(newValue: Boolean)
    {
        highEndPad.isEnabled = newValue
        crossoverFreq.isEnabled = newValue
    }
}