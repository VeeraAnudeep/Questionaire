package com.ab.questionnaire

/**
 * Created on 20/08/18.
 */
public class Question {
    private var question: String = ""
    private var recorded: Boolean = false
    private var skipped: Boolean = false
    private var canAttempt: Boolean = false
    private var inRecording: Boolean = false

    constructor(question: String, recorded: Boolean) {
        this.question = question
        this.recorded = recorded
    }

    fun setRecorded(isRecored: Boolean) {
        inRecording = false
        this.recorded = isRecored
    }

    fun isRecorded(): Boolean {
        return recorded
    }

    fun setSkipped(skipped: Boolean) {
        this.skipped = skipped
    }

    fun setInRecording(inRecording: Boolean) {
        this.inRecording = inRecording
    }

    fun isInRecording(): Boolean {
        return inRecording
    }

    fun isSkipped(): Boolean {
        return skipped
    }

    fun setCanAttempt(canAttempt: Boolean) {
        this.canAttempt = canAttempt
    }

    fun canAttempt(): Boolean {
        return canAttempt && !recorded && !skipped
    }

    fun getQuestion(): String {
        return question
    }
}