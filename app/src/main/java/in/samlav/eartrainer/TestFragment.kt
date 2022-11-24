package `in`.samlav.eartrainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import `in`.samlav.eartrainer.databinding.FragmentTestBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TestFragment : Fragment()
{

    private var _binding: FragmentTestBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {

        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToHome.setOnClickListener {
            findNavController().navigate(R.id.action_TestFragment_to_HomeFragment)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}