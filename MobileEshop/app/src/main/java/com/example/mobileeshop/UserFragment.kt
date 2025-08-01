package com.example.mobileeshop
//import απαραίτητων βιβλιοθηκών
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//κλάση γιο χρήστη
class UserFragment : Fragment() {

    //ui για κουμπιά χρήστη
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        //πεδίο για εμφάνιση κουμπιών
        usernameTextView = view.findViewById(R.id.usernameEditText)
        emailTextView = view.findViewById(R.id.emailEditText)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        usernameTextView.isEnabled = false
        emailTextView.isEnabled = false

        //φόρτωση πληροφοριών χρήστη
        loadUserData()

        //αλλαγή κωδικου
        changePasswordButton.setOnClickListener {
            sendPasswordReset()
        }

        //διαγραφή λογαριαμσού
        deleteAccountButton.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_account_title))
                    .setMessage(getString(R.string.delete_account_message))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        val uid = user.uid

                        firestore.collection("users").document(uid).delete()
                            .addOnSuccessListener {
                                user.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        })
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), getString(R.string.auth_delete_failed, it.message), Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), getString(R.string.firestore_delete_failed, it.message), Toast.LENGTH_LONG).show()
                            }
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }

        //κουμπι αποσύνδεσης
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        return view
    }

    //συνάρτηση φόρτωσης στοιχείων χρήστη
    private fun loadUserData() {
        val user = auth.currentUser ?: return
        val docRef = firestore.collection("users").document(user.uid)

        docRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    usernameTextView.text = doc.getString("username") ?: ""
                    emailTextView.text = doc.getString("email") ?: ""
                } else {
                    Toast.makeText(requireContext(), getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.failed_to_load_user_data), Toast.LENGTH_SHORT).show()
            }
    }

    //συνλαρτηση αποστολής mail για επαναφορά κωδικού
    private fun sendPasswordReset() {
        val email = emailTextView.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.email_empty), Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.reset_email_failed, it.message), Toast.LENGTH_SHORT).show()
            }
    }

    //επιβεβαίωση δφιαγραφής λογαριασμού
    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccount()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    //συνάρτηση διαγραφής λογαριασμσμού
    private fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        user.delete()
            .addOnSuccessListener {
                firestore.collection("users").document(uid).delete()
                Toast.makeText(requireContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.failed_to_delete_account, it.message), Toast.LENGTH_LONG).show()
            }
    }
}
