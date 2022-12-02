package `in`.samlav.eartrainer

import `in`.samlav.eartrainer.databinding.FragmentTestBinding
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlin.math.sin


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

    private val sampleRate = 8000
    private lateinit var generatedSnd: ByteArray

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
                if (!preferences.getBoolean("showExcluded", false))
                {
                    continue
                }
                button.isEnabled = false
            }
            binding.root.findViewById<LinearLayout>(R.id.bin_container).addView(button)
        }

        // TODO: Test Logic

        playTone(440, 25)

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

    private fun playTone(freq: Int, length: Int)
    {
        Thread {
            genTone(freq, length)
            Handler(Looper.getMainLooper()).post { playSound() }
        }.start()
    }

    private fun genTone(freq: Int, duration: Int)
    {
        val seconds = duration.toFloat() / 100f
        val numSamples = (seconds * sampleRate).toInt()
        generatedSnd = ByteArray(2 * numSamples)
        val sample = DoubleArray(numSamples)

        // fill out the array
        for (i in 0 until numSamples)
        {
            sample[i] = sin(2 * Math.PI * i / (sampleRate / freq))
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        var idx = 0
        for (dVal in sample)
        {
            // scale to maximum amplitude
            val `val` = (dVal * 32767).toInt()
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (`val` and 0x00ff).toByte()
            generatedSnd[idx++] = ((`val` and 0xff00) ushr 8).toByte()
        }
    }

    private fun playSound()
    {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
    }
}