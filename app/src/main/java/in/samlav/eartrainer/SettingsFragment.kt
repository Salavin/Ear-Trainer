package `in`.samlav.eartrainer

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.preference.*
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
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
    lateinit var about: Preference

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
        about=findPreference("about")!!

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            backPressed()
        }

        whichBins.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {_, _ ->
            if (checkWhichBins())
            {
                Toast.makeText(context, getString(R.string.which_bins_error), Toast.LENGTH_LONG).show()
            }
            return@OnPreferenceChangeListener true
        }
        numBins.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            changeBins(newValue as String)
            return@OnPreferenceChangeListener true
        }
        useEQ.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            onUseEQChange(newValue as Boolean)
            return@OnPreferenceChangeListener true
        }
        about.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
            return@setOnPreferenceClickListener true
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

    /**
     * Helper function to determine if the user has selected all of the bins for the Which Bins to Exclude [Preference].
     *
     * @return true if not all of the bins are selected, false otherwise
     */
    fun checkWhichBins(): Boolean
    {
        for (bin: CharSequence in whichBins.entries)
        {
            if (!whichBins.values.contains(bin.toString()))
            {
                return true
            }
        }
        return false
    }

    /**
     * Handles when the User attempts to leave the [SettingsFragment]. If all of the bins are selected in the Which Bins to Exclude, it will not let them exit, and instead throw and error in the form of a [Toast].
     *
     * @return true
     */
    fun backPressed(): Boolean
    {
        if (checkWhichBins())
        {
            findNavController().navigate(R.id.action_settingsFragment_to_HomeFragment)
        }
        else
        {
            Toast.makeText(context, getString(R.string.which_bins_error), Toast.LENGTH_LONG).show()
        }
        return true
    }
}