package com.example.geoquiz

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "GeoQuizViewModel"

class GeoQuizViewModel : ViewModel() {

    var mIsCheater: Boolean = false
    var mCurrentIndex: Int = 0
    private val mQuestionBank = mutableListOf(
        Question(R.string.question_1, R.string.c_question_1, mutableListOf(R.string.i1_question_1, R.string.c_question_1, R.string.i2_question_1, R.string.i3_question_1)),
        Question(R.string.question_2, R.string.c_question_2, mutableListOf(R.string.c_question_2, R.string.i1_question_2, R.string.i2_question_2, R.string.i3_question_2)),
        Question(R.string.question_3, R.string.c_question_3, mutableListOf(R.string.i1_question_3, R.string.i2_question_3, R.string.i3_question_3, R.string.c_question_3)),
    )

    val currentQuestionAnswer: Int
        get() = mQuestionBank[mCurrentIndex].mCorrectAnswer
    val currentQuestionText: Int
        get() = mQuestionBank[mCurrentIndex].mQuestion
    fun moveToNext() {
        mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.size
    }
    fun moveToPrevious() {
        mCurrentIndex = ((mCurrentIndex - 1) + mQuestionBank.size) % mQuestionBank.size
    }
    fun answerAtButtonPosition(position: Int): Int{
        return mQuestionBank[mCurrentIndex].mAnswers[position]
    }
}