package `in`.samlav.eartrainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import `in`.samlav.eartrainer.databinding.FragmentHomeBinding
import android.content.SharedPreferences
import android.widget.Toast
import androidx.preference.PreferenceManager

/**
 * Home fragment
 *
 * @constructor Create empty Home fragment
 */
class HomeFragment : Fragment()
{
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToTest.setOnClickListener {
            val whichBins = preferences.getStringSet("whichBins", null)!!
            val whichBinsEntries = when (preferences.getString("numBins", "31"))
            {
                "10" -> resources.getStringArray(R.array.ten_bin_entries)
                "15" -> resources.getStringArray(R.array.fifteen_bin_entries)
                "20" -> resources.getStringArray(R.array.twenty_bin_entries)
                "31" -> resources.getStringArray(R.array.thirty_one_bin_entries)
                else -> resources.getStringArray(R.array.thirty_one_bin_entries)
            }
            var whichBinsError = true
            for (bin in whichBinsEntries)
            {
                if (!whichBins.contains(bin))
                {
                    findNavController().navigate(R.id.action_HomeFragment_to_TestFragment)
                    whichBinsError = false
                }
            }
            if (whichBinsError)
            {
                Toast.makeText(context, getString(R.string.which_bins_error), Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_HomeFragment_to_settingsFragment)
            }
        }
        binding.buttonToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_settingsFragment)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}