package com.devourer.alexb.diaryforthecoolestboys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.realm.Realm

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "Main"
        const val INTENT_ID = "auth"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this@SplashActivity)

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }


    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
                val intent = Intent(this,MainActivity::class.java)
                intent.putExtra(INTENT_ID,false)
                startActivity(intent)
                finish()
        }
        else{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
