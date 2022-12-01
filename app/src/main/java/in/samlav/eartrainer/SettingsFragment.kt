package `in`.samlav.eartrainer

import android.os.Bundle
import android.widget.SeekBar
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat()
{
    lateinit var numBins: ListPreference
    lateinit var whichBins: MultiSelectListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        numBins = findPreference("numBins")!!
        whichBins = findPreference("whichBins")!!
        numBins.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            changeBins(newValue as String)
            return@OnPreferenceChangeListener true
        }
        changeBins(numBins.value)
    }

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
}