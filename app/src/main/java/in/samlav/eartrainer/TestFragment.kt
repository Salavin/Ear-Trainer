package `in`.samlav.eartrainer

import `in`.samlav.eartrainer.databinding.FragmentTestBinding
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.media.AudioAttributes.*
import android.media.AudioFormat
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
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
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


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

    private val sampleRate = 44100
    private lateinit var generatedSnd: ByteArray

    private var questionNum = 0
    private lateinit var questionThread: Thread
    private lateinit var possibleBins: MutableList<Int>
    private lateinit var buttons: MutableList<Button>
    private var timeLeft = 10
    private var triesLeft = 1
    private var curFreqBin = 0
    private var useTimer = true
    private var timerEnabled = false
    private var numCorrect = 0
    private lateinit var timerThread: Thread
    private lateinit var audioTrack: AudioTrack
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            backPressed()
        }

        // The callback can be enabled or disabled here or in the lambda
    }

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
        possibleBins = mutableListOf()
        buttons = mutableListOf()
        for ((i, bin: String) in array.withIndex())
        {
            val button = Button(context)
            button.id = i
            button.text = resources.getString(R.string.hz).format(bin)
            val freq = freqTextToVal(button.text as String)
            button.setOnClickListener { buttonPress(freq, button) }
            if (preferences.getStringSet("whichBins", null)?.contains(bin) == true)
            {
                if (!preferences.getBoolean("showExcluded", false))
                {
                    continue
                }
                button.isEnabled = false
            }
            else
            {
                possibleBins.add(freq)
            }
            binding.root.findViewById<LinearLayout>(R.id.bin_container).addView(button)
            buttons.add(button)
        }

        // TODO: Test Logic

        // Setup

        val numQuestions = when (preferences.getString("numQuestions", "10"))
        {
            "inf" -> Int.MAX_VALUE
            else -> preferences.getString("numQuestions", "10").toString().toInt()
        }

        if (preferences.getString("timetoAnswer", "10") == "inf")
        {
            binding.root.findViewById<TextView>(R.id.time_time_left).text = "∞"
            useTimer = false
        }

        binding.root.findViewById<TextView>(R.id.number_score).text = resources.getString(R.string.questionNum, 0, 0.toString())

        // Thread defs

        timerThread = Thread {
            if (timerEnabled)
            {
                binding.root.findViewById<TextView>(R.id.time_time_left).text =
                    (--timeLeft).toString()
                if (timeLeft == 0)
                {
                    binding.root.findViewById<TextView>(R.id.number_score).text = resources.getString(R.string.questionNum, numCorrect, questionNum.toString())
                    disableButtons(
                        preferences.getBoolean("immediateFeedback", true),
                        curFreqBin,
                        null
                    )
                    Handler(Looper.getMainLooper()).postDelayed(questionThread, preferences.getInt("feedbackTime", 3) * 1000L)
                }
                else
                {
                    Handler(Looper.getMainLooper()).postDelayed(timerThread, 1000)
                }
            }
        }

        questionThread = Thread {
            enableButtons()
            if (++questionNum <= numQuestions)
            {
                setQuestionNumText(questionNum, !useTimer)

                curFreqBin = possibleBins.random()
                val freq: Int = if (preferences.getInt("deviation", 0) > 0)
                {
                    (curFreqBin * (((Random.nextInt(0, preferences.getInt("deviation", 0)) * randomPlusOrMinus()).toFloat() / 100f) + 1)).roundToInt()
                } else
                {
                    curFreqBin
                }
                playTone(freq, preferences.getInt("toneTime", 1000))
                triesLeft = preferences.getInt("numTries", 1)
                binding.root.findViewById<TextView>(R.id.number_try).text = triesLeft.toString()
                if (useTimer)
                {
                    binding.root.findViewById<TextView>(R.id.time_time_left).text = "--"
                    timeLeft = preferences.getString("timeToAnswer", "10")?.toInt()!!
                    timerEnabled = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (timerEnabled)
                        {
                            binding.root.findViewById<TextView>(R.id.time_time_left).text =
                                preferences.getString("timeToAnswer", "10")
                        }}, preferences.getInt("toneTime", 1000).toLong())
                    Handler(Looper.getMainLooper()).postDelayed(timerThread, preferences.getInt("toneTime", 1000).toLong() + 1000)
                }
                else
                {
                    binding.root.findViewById<TextView>(R.id.time_time_left).text = "∞"
                }
            }
            else
            {
                timerEnabled = false
                if (this::dialog.isInitialized && dialog.isShowing)
                {
                    dialog.cancel()
                }
                val bundle = bundleOf(ARG_NUM_CORRECT to numCorrect, ARG_NUM_QUESTIONS to numQuestions)
                findNavController().navigate(R.id.action_TestFragment_to_resultsFragment, bundle)
            }
        }

        // Loop

        questionThread.start()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    private fun buttonPress(freq: Int, button: Button)
    {
        binding.root.findViewById<TextView>(R.id.number_try).text = (--triesLeft).toString()
        if (freq == curFreqBin)
        {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING)
            {
                audioTrack.stop()
            }
            timerEnabled = false
            binding.root.findViewById<TextView>(R.id.number_score).text = resources.getString(R.string.questionNum, ++numCorrect, questionNum.toString())
            disableButtons(preferences.getBoolean("immediateFeedback", true), curFreqBin, null)
            Handler(Looper.getMainLooper()).postDelayed(questionThread, preferences.getInt("feedbackTime", 3) * 1000L)
        }
        else if (triesLeft == 0)
        {
            timerEnabled = false
            binding.root.findViewById<TextView>(R.id.number_score).text = resources.getString(R.string.questionNum, numCorrect, questionNum.toString())
            disableButtons(preferences.getBoolean("immediateFeedback", true), curFreqBin, freq)
            Handler(Looper.getMainLooper()).postDelayed(questionThread, preferences.getInt("feedbackTime", 3) * 1000L)
        }
        else
        {
            button.background.colorFilter = BlendModeColorFilter(context?.getColor(R.color.red)!!, BlendMode.MULTIPLY)
            button.isEnabled = false
        }
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
        val seconds = duration.toFloat() / 1000f
        val numSamples = (seconds * sampleRate.toFloat()).toInt()
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
        val audioAttributes = with (Builder())
        {
            setUsage(USAGE_MEDIA)
            setContentType(CONTENT_TYPE_SONIFICATION)
            setFlags(FLAG_AUDIBILITY_ENFORCED)
            build()
        }
        val audioFormat = with (AudioFormat.Builder())
        {
            setEncoding(ENCODING_PCM_16BIT)
            setSampleRate(sampleRate)
            setChannelMask(CHANNEL_OUT_MONO)
            build()
        }
        audioTrack = AudioTrack(audioAttributes, audioFormat, generatedSnd.size, AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE)
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
    }

    private fun randomPlusOrMinus(): Int
    {
        val array = arrayOf(-1, 1)
        return array.random()
    }

    private fun setQuestionNumText(num: Int, isInf: Boolean)
    {
        if (isInf)
        {
            binding.root.findViewById<TextView>(R.id.number_question).text = resources.getString(R.string.questionNum, num, "∞")
        }
        else
        {
            binding.root.findViewById<TextView>(R.id.number_question).text = resources.getString(R.string.questionNum, num, preferences.getString("numQuestions", "10"))
        }
    }

    private fun disableButtons(showFeedback: Boolean, correctFreq: Int?, incorrectFreq: Int?)
    {
        for (button in buttons)
        {
            if (showFeedback)
            {
                if (freqTextToVal(button.text as String) == incorrectFreq)
                {
                    button.background.colorFilter = BlendModeColorFilter(context?.getColor(R.color.red)!!, BlendMode.MULTIPLY)
                    button.isEnabled = false
                }
                else if (freqTextToVal(button.text as String) == correctFreq)
                {
                    button.background.colorFilter = BlendModeColorFilter(context?.getColor(R.color.green)!!, BlendMode.MULTIPLY)
                    button.isClickable = false
                }
                else
                {
                    button.isEnabled = false
                }
            }
        }
    }

    private fun enableButtons()
    {
        for (button in buttons)
        {
            if (preferences.getStringSet("whichBins", null)?.contains(button.text) == false)
            {
                button.isEnabled = true
                button.isClickable = true
                button.background.colorFilter = null
            }
        }
    }

    private fun freqTextToVal(freq: String): Int
    {
        return if (freq.contains('k'))
        {
            (freq.substring(0, freq.length - 3).toFloat() * 1000).toInt()
        }
        else if (freq.contains('.'))
        {
            freq.substring(0, freq.length - 4).toInt()  // Yes, this truncates the decimals, but no one is going to tell the difference between half a hertz.
        }
        else
        {
            freq.substring(0, freq.length - 2).toInt()
        }
    }

    fun backPressed(): Boolean
    {
        dialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Exit Test?")
                setMessage("Are you sure you want to exit the test? Your progress will be lost.")
                setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    timerEnabled = false
                    audioTrack.stop()
                    findNavController().navigate(R.id.action_TestFragment_to_HomeFragment)
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }

            // Create the AlertDialog
            builder.create()
        }!!
        dialog.show()
        return true
    }
}