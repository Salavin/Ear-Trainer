package `in`.samlav.eartrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment()
{
    private lateinit var websiteButton: Button
    private lateinit var repositoryButton: Button
    private lateinit var buyMeACoffeeButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        websiteButton = view.findViewById(R.id.button_website)
        repositoryButton = view.findViewById(R.id.button_project_repository)
        buyMeACoffeeButton = view.findViewById(R.id.button_buy_me_a_coffee)
        websiteButton.setOnClickListener {
            goToUrl("https://samlav.in")
        }
        repositoryButton.setOnClickListener {
            goToUrl("https://github.com/Salavin/Ear-Trainer")
        }
        buyMeACoffeeButton.setOnClickListener {
            goToUrl("https://www.buymeacoffee.com/salavin")
        }
    }

    /**
     * Helper function to send the User to a website.
     *
     * @param url the URL of the website to send the User to
     */
    private fun goToUrl (url: String)
    {
        val uriUrl = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }
}