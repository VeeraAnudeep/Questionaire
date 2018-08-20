package com.ab.questionnaire

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.p_question_item.view.*


/**
 * Created on 19/08/18.
 */
class QuestionsAdapter(val uploadList: UploadList) : RecyclerView.Adapter<QuestionsAdapter
.QuestionViewHolder>() {

    private var questions: ArrayList<Question> = ArrayList()
    private var myAudioRecorder: MediaRecorder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        return QuestionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout
                .p_question_item, parent, false))
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    fun setQuestions(questions: ArrayList<Question>) {
        this.questions = questions
        questions[0].setCanAttempt(true)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val questionObject = questions[position]
        holder.itemView.textView2.text = questionObject.getQuestion()
        val outputFile = Environment.getExternalStorageDirectory().absolutePath +
                "/q_${position+1}.aac"
        if (questionObject.canAttempt()) {
            if (questionObject.isInRecording()) {
                holder.itemView.stop.visibility = View.VISIBLE
                holder.itemView.button.visibility = View.GONE
                holder.itemView.play.visibility = View.GONE
                holder.itemView.skip.visibility = View.GONE
            } else {
                holder.itemView.button.visibility = View.VISIBLE
                holder.itemView.skip.visibility = View.VISIBLE
            }
        } else {
            holder.itemView.button.visibility = View.GONE
            holder.itemView.skip.visibility = View.GONE
            if (questionObject.isRecorded()) {
                holder.itemView.stop.visibility = View.GONE
                holder.itemView.play.visibility = View.VISIBLE
            } else {
                holder.itemView.play.visibility = View.GONE
            }
        }
        holder.itemView.skip.setOnClickListener {
            questionObject.setSkipped(true)
            if (position != questions.size - 1) {
                questions[position + 1].setCanAttempt(true)
                notifyItemChanged(position+1)
            }
            notifyItemChanged(position)
        }
        holder.itemView.button.setOnClickListener {
            holder.itemView.stop.visibility = View.VISIBLE
            holder.itemView.button.visibility = View.GONE
            holder.itemView.skip.visibility = View.GONE
            myAudioRecorder = MediaRecorder()
            myAudioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            myAudioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            myAudioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            myAudioRecorder?.setOutputFile(outputFile)
            myAudioRecorder?.prepare()
            myAudioRecorder?.start()
            questionObject.setInRecording(true)
            notifyItemChanged(position)
        }
        holder.itemView.stop.setOnClickListener {
            myAudioRecorder?.stop()
            myAudioRecorder?.release()
            myAudioRecorder = null
            questionObject.setRecorded(true)
            holder.itemView.stop.visibility = View.GONE
            holder.itemView.play.visibility = View.VISIBLE
            if (position != questions.size - 1) {
                questions[position + 1].setCanAttempt(true)
                notifyItemChanged(position+1)
            }
            uploadList.setFileURI(outputFile)
            notifyItemChanged(position)
        }
        holder.itemView.play.setOnClickListener {
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(outputFile)
                mediaPlayer.prepare()
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mediaPlayer2 ->
                    mediaPlayer2.release()
                }
            } catch (e: Exception) {
                // make something
                mediaPlayer.release()
            }
        }
    }

    class QuestionViewHolder(item: View) : RecyclerView.ViewHolder(item)
}