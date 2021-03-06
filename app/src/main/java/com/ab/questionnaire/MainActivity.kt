package com.ab.questionnaire

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), UploadList {
    override fun setFileURI(filePath: String, questionIndex: Int) {
        responses[questionIndex] = Uri.fromFile(File(filePath))
        submit.visibility = View.VISIBLE
    }
    private val PHONE_MATCHER = "\\d{10}"
    private var mStorageRef: StorageReference? = null
    private var adapter: QuestionsAdapter? = null
    private var responses: HashMap<Int,Uri> = HashMap()
    val TAG: String = "Here"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest
                        .permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission
                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), 1)
        }
        // If there's an upload in progress, save the reference so you can query it later
        if (mStorageRef != null) {
            savedInstanceState?.putString("reference", mStorageRef.toString())
        }
        setContentView(R.layout.activity_main)
        mStorageRef = FirebaseStorage.getInstance().reference
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = QuestionsAdapter(this)
        recyclerView.adapter = adapter
        fetchQuestions()

        submit.setOnClickListener {
            if(editText.text.trim().toString().isNotEmpty()
                    && file_name.text.trim().toString().isNotEmpty()) {
                    uploadFiles(editText.text.trim().toString().toLowerCase(), file_name.text.trim().toString())
            }else{
                Toast.makeText(applicationContext,"Please enter all details",Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // If there was an upload in progress, get its reference and create a new StorageReference
        val stringRef = savedInstanceState.getString("reference") ?: return
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef)

        // Find all UploadTasks under this StorageReference
        val tasks = mStorageRef?.activeUploadTasks
        tasks?.let {
            if (tasks.size > 0) {
                // Get the task monitoring the upload
                val task = tasks[0]

                // Add new listeners to the task using an Activity scope
                task.addOnSuccessListener(this) { state ->
                    //                    handleSuccess(state) //call a user defined function to handle the event.
                }
            }
        }

    }

    private fun fetchQuestions() {
        FirebaseDatabase.getInstance().getReference("questions")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach {

                        }
                        val questions = dataSnapshot.value as ArrayList<String>
                        val questionArray = ArrayList<Question>()
                        for (question in questions) {
                            val questionItem = Question(question, false)
                            questionArray.add(questionItem)
                        }
                        adapter?.setQuestions(questions = questionArray)
                        editText.visibility = View.VISIBLE
                        file_name.visibility = View.VISIBLE
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }


    private fun uploadFiles(toString: String, phone: String) {
        if(responses.size>0) {
            submit.text = "Submitting.."
        }
        for (key in responses.keys) {
            val responseRef = mStorageRef?.child("response_${toString}_$phone")?.child("q_${key +
                    1}.aac")
            responses[key]?.let {
                responseRef?.putFile(it)
                        ?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                            // Get a URL to the uploaded content
                            submit.text = "Resubmit!"
                            Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
                        })
                        ?.addOnFailureListener(OnFailureListener {
                            // Handle unsuccessful uploads
                            // ...
                            submit.text = "Submit"
                            Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
                        })
            }
        }
    }
}
