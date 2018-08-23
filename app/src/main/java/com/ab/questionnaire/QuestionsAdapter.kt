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
    private var recordingInProgress: Boolean = false
    private var playingRecording: Boolean = false

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
                "/q_${position + 1}.aac"
        if (questionObject.isInRecording()) {
            holder.itemView.stop.visibility = View.VISIBLE
            holder.itemView.record.visibility = View.GONE
            holder.itemView.play.visibility = View.GONE
            holder.itemView.skip.visibility = View.GONE
            holder.itemView.re_record.visibility = View.GONE
        } else if (questionObject.canAttempt()) {
            holder.itemView.record.visibility = View.VISIBLE
            holder.itemView.skip.visibility = View.VISIBLE
            holder.itemView.re_record.visibility = View.GONE
        } else {
            holder.itemView.record.visibility = View.GONE
            holder.itemView.skip.visibility = View.GONE
            if (questionObject.isRecorded()) {
                holder.itemView.stop.visibility = View.GONE
                holder.itemView.play.visibility = View.VISIBLE
            } else {
                holder.itemView.play.visibility = View.GONE
            }
            if (questionObject.isSkipped() || questionObject.isRecorded()) {
                holder.itemView.re_record.visibility = View.VISIBLE
            } else {
                holder.itemView.re_record.visibility = View.GONE
            }
        }
        holder.itemView.skip.setOnClickListener {
            questionObject.setSkipped(true)
            if (position != questions.size - 1) {
                questions[position + 1].setCanAttempt(true)
                notifyItemChanged(position + 1)
            }
            notifyItemChanged(position)
        }
        var mediaPlayer = MediaPlayer()
        holder.itemView.record.setOnClickListener {
            record(holder, outputFile, questionObject, position)
        }
        holder.itemView.re_record.setOnClickListener {
            record(holder, outputFile, questionObject, position)
        }
        holder.itemView.stop.setOnClickListener {
            if (playingRecording) {
                mediaPlayer.stop()
                mediaPlayer.release()
                playingRecording = false
                holder.itemView.stop.visibility = View.GONE
                holder.itemView.play.visibility = View.VISIBLE
            } else if (recordingInProgress) {
                myAudioRecorder?.stop()
                myAudioRecorder?.release()
                myAudioRecorder = null
                recordingInProgress = false
                questionObject.setRecorded(true)
                holder.itemView.stop.visibility = View.GONE
                holder.itemView.play.visibility = View.VISIBLE
                if (position != questions.size - 1) {
                    questions[position + 1].setCanAttempt(true)
                    notifyItemChanged(position + 1)
                }
                uploadList.setFileURI(outputFile,position)
            }
            notifyItemChanged(position)
        }
        holder.itemView.play.setOnClickListener {
            try {
                if (!recordingInProgress) {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(outputFile)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    holder.itemView.stop.visibility = View.VISIBLE
                    holder.itemView.play.visibility = View.GONE
                    playingRecording = true
                    mediaPlayer.setOnCompletionListener { mediaPlayer2 ->
                        playingRecording = false
                        mediaPlayer2.release()
                        holder.itemView.play.visibility = View.VISIBLE
                        holder.itemView.stop.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // make something
                mediaPlayer.release()
            }
        }
    }

    fun record(holder: QuestionViewHolder, outputFile: String, questionObject: Question, position: Int) {
        if (!playingRecording && !recordingInProgress) {
            holder.itemView.stop.visibility = View.VISIBLE
            holder.itemView.record.visibility = View.GONE
            holder.itemView.skip.visibility = View.GONE
            myAudioRecorder = MediaRecorder()
            myAudioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            myAudioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            myAudioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            myAudioRecorder?.setOutputFile(outputFile)
            myAudioRecorder?.prepare()
            myAudioRecorder?.start()
            recordingInProgress = true
            questionObject.setInRecording(true)
            notifyItemChanged(position)
        }
    }

    class QuestionViewHolder(item: View) : RecyclerView.ViewHolder(item)
}