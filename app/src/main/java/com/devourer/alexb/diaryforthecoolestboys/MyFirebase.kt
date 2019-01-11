package com.devourer.alexb.diaryforthecoolestboys

import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class MyFirebase (context: Context){

    companion object {
        private const val TAG = "Main"
    }

    val mAuth = FirebaseAuth.getInstance()
    private val myDB = FirebaseFirestore.getInstance()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    var mGoogleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
    private val uId = mAuth.uid!!
    private val user = mAuth.currentUser!!
    val name = user.displayName!!
    val email = user.email!!
    val photoUrl = user.photoUrl!!
    //Database links
    private val users = myDB.collection("users")
    val uIdDoc = users.document(uId)
    //

    fun addTask(map: Map<String, Any?>, id: String?){
        uIdDoc.collection(NavMenuCheckedItem.title).document(id!!).set(map).addOnCompleteListener{
            if (it.isSuccessful){
                Log.w(TAG,"Добавление таски в базу Firebase суксесфул")
            }
        }
    }

    fun changeTask(map: Map<String,Any?>, id: String?){
        Log.w(TAG,"ID (fire) -> $id")

        uIdDoc.collection(NavMenuCheckedItem.title).document(id!!).update(map).addOnCompleteListener {
            if (it.isSuccessful){
                Log.w(TAG,"Изменение таски в базе Firebase суксесфул")
            }
        }
    }

    fun deleteTask(id: String?){

        uIdDoc.collection(NavMenuCheckedItem.title).document(id!!).delete().addOnCompleteListener {
            if (it.isSuccessful){
                Log.w(TAG, "Удаление таски в базе Firebase successful")
            }
        }
    }

    fun deleteAllCompletedTasks(completedTasks: ArrayList<CompletedTask>){

        for ((i) in (0 until completedTasks.size).withIndex()){
            uIdDoc.collection(NavMenuCheckedItem.title).document(completedTasks[i].id!!).delete().addOnCompleteListener {
                if (it.isSuccessful){

                }
            }
        }


    }

    fun addTaskToCompleted(id: String?, completionDate: Any?){

        uIdDoc.collection(NavMenuCheckedItem.title).document(id!!).update(
            mapOf(
                "key" to true,
                "completion_date" to completionDate,
                "date" to Date()
            )
        ).addOnCompleteListener {
            if (it.isSuccessful){
                Log.w(TAG, "Добавление таски в список завершённых successful")
            }
        }

    }

    fun addTaskToNotCompleted(map: Map<String, Any?>, id: String?){
        uIdDoc.collection(NavMenuCheckedItem.title).document(id!!).set(map).addOnCompleteListener {
            if(it.isSuccessful){
                Log.w(TAG, "Добавление таски в список незавершённых successful")
            }
        }
    }


    fun deleteTaskList(title: String){

        uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").document(title).delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {


                }
            }
        uIdDoc.collection(title).get().addOnCompleteListener {
            if(it.isSuccessful){
                it.result!!.documents.forEach { doc ->
                    doc.reference.delete()
                }
            }
        }
    }

    fun addTaskList(taskListName: String, map: Map<String, Any?>){
        uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").document(taskListName).set(map)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.w(TAG, "MyFirebase | successful adding list")
                }
                else{
                    Log.w(TAG, "MyFirebase addTaskList ERROR -> ${it.exception}")

                }
            }
    }




}