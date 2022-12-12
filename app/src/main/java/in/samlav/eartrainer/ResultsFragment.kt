package `in`.samlav.eartrainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController

// the fragment initialization parameters
const val ARG_NUM_CORRECT = "score"
const val ARG_NUM_QUESTIONS = "num_questions"

/**
 * Results fragment. Takes two args in the form of a [Bundle]: the number of correct questions, and the number of total questions.
 *
 * @constructor Create empty Results fragment
 */
class ResultsFragment : Fragment()
{
    private var numCorrect: Int? = null
    private var numQuestions: Int? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numCorrect = it.getInt(ARG_NUM_CORRECT)
            numQuestions = it.getInt(ARG_NUM_QUESTIONS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        view.findViewById<TextView>(R.id.number_final_score).text = resources.getString(R.string.questionNum, numCorrect, numQuestions.toString())
        view.findViewById<Button>(R.id.button_retry).setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_TestFragment)
        }
        view.findViewById<Button>(R.id.button_to_home).setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_HomeFragment)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}