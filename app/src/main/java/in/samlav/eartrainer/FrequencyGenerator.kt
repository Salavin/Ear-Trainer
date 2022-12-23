package `in`.samlav.eartrainer

import kotlin.math.pow
import kotlin.math.sin

class FrequencyGenerator(freq: Int, duration: Int, val sampleRate: Int = 44100)
{
    lateinit var generatedSnd: ByteArray // Initializer required, not a nullable type
        private set // the setter is private and has the default implementation
    private var pad = 0
    private var crossoverFreq = 2000

    constructor(freq: Int, duration: Int, pad: Int, crossoverFreq: Int, sampleRate: Int = 44100): this(freq, duration, sampleRate)
    {
        this.pad = pad
        this.crossoverFreq = crossoverFreq
        genTone(freq, duration)
    }

    init
    {
        genTone(freq, duration)
    }


    /**
     * Generate the sine wave that represents a specific [freq].
     *
     * Originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
     * and modified by Steve Pomeroy <steve@staticfree.info>.
     * Adapted for Kotlin by Sam Lavin.
     *
     * @param freq the frequency for the sine wave
     * @param duration the length of the sine wave (in ms)
     */
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
            // scale to amplitude
            val `val` = (dVal * calculateP(calculateAmplitudeModifier(freq))).toInt()
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (`val` and 0x00ff).toByte()
            generatedSnd[idx++] = ((`val` and 0xff00) ushr 8).toByte()
        }
    }

    /**
     * Calculates by how many decibels we should modify the amplitude based on what frequency we are playing.
     *
     * @param freq the frequency we are playing
     * @return a [Float] between 0 and the High End Pad set in Preferences
     */
    private fun calculateAmplitudeModifier(freq: Int): Float
    {
        if ((pad != 0) && (freq > crossoverFreq))
        {
            val transitionBand = crossoverFreq / 2

            /* Check if we are in the transition band */
            if (freq < crossoverFreq + transitionBand)
            {
                return ((freq - crossoverFreq).toFloat() / transitionBand.toFloat()) * pad.toFloat()
            }
            /* Else */
            return pad.toFloat()
        }
        /* Else */
        return 0f
    }

    /**
     * Helper method to calculate the amplitude for the sine wave based on the pad amount
     *
     * @param pad the amount to pad the amplitude by
     * @return the padded (or not) amplitude
     */
    private fun calculateP(pad: Float): Float
    {
        val p = 32767*(10f.pow(-pad /10f))
        return p
    }
}