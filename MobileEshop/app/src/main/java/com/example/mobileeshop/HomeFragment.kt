package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace

//κλάση για την αρχική σελίδα
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ανακατεύθυνση στο καλάθι
        view.findViewById<ImageButton>(R.id.btnProducts)
            .setOnClickListener {
                parentFragmentManager.commit {
                    replace<ShoppingListFragment>(R.id.container)
                    addToBackStack(null)
                }
            }

        // ανακατεύθυνση στο Wishlist
        view.findViewById<ImageButton>(R.id.btnWishlist)
            .setOnClickListener {
                parentFragmentManager.commit {
                    replace<WishlistFragment>(R.id.container)
                    addToBackStack(null)
                }
            }

        // ανακατεύθυνση στο History
        view.findViewById<ImageButton>(R.id.btnHistory)
            .setOnClickListener {
                parentFragmentManager.commit {
                    replace<HistoryFragment>(R.id.container)
                    addToBackStack(null)
                }
            }

        // ανακατεύθυνση στο Offers
        view.findViewById<ImageButton>(R.id.btnOffers)
            .setOnClickListener {
                parentFragmentManager.commit {
                    replace<OffersFragment>(R.id.container)
                    addToBackStack(null)
                }
            }
    }
}
