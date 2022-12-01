package `in`.samlav.eartrainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.samlav.eartrainer.databinding.FragmentTestBinding
import android.content.SharedPreferences
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.preference.PreferenceManager

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TestFragment : Fragment()
{

    private var _binding: FragmentTestBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var preferences: SharedPreferences
    private var numBins = 0
    private lateinit var array: Array<out String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }!!
        numBins = preferences.getString("numBins", "10")?.toInt()!!
        array = when (numBins)
        {
            10 -> resources.getStringArray(R.array.ten_bin_entries)
            15 -> resources.getStringArray(R.array.fifteen_bin_entries)
            20 -> resources.getStringArray(R.array.twenty_bin_entries)
            31 -> resources.getStringArray(R.array.thirty_one_bin_entries)
            else -> resources.getStringArray(R.array.ten_bin_entries)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        for ((i, bin: String) in array.withIndex())
        {
            val button = Button(context)
            button.id = i
            button.text = resources.getString(R.string.hz).format(bin)
            button.setOnClickListener { it as Button
                val freq: Int = if (it.text.contains('k'))
                {
                    (it.text.substring(0, it.text.length - 3).toFloat() * 1000).toInt()
                }
                else
                {
                    it.text.substring(0, it.text.length - 2).toInt()
                }
                buttonPress(freq)
            }
            if (preferences.getStringSet("whichBins", null)?.contains(bin) == true)
            {
                button.isEnabled = false
            }
            binding.root.findViewById<LinearLayout>(R.id.bin_container).addView(button)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    private fun buttonPress(freq: Int)
    {
        Toast.makeText(context, "You pressed: $freq", Toast.LENGTH_SHORT).show()
    }
}