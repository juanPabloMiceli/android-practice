package com.example.geoquiz

import android.app.ActionBar
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var mAnswer1: Button
    private lateinit var mAnswer2: Button
    private lateinit var mAnswer3: Button
    private lateinit var mAnswer4: Button
    private lateinit var mQuestionTextView: TextView
    private lateinit var mNextButton: ImageButton
    private lateinit var mPrevButton: ImageButton
    private lateinit var mCheatButton: Button
    private var mIsCheater: Boolean = false
    private var mCurrentIndex: Int = 0
    private val TAG: String = "QuizActivity"
    private val INDEX_KEY: String = "indexKey"
    private val CHEATER_KEY: String = "cheaterKey"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Creating activity")
        supportActionBar?.subtitle = "API level ${Build.VERSION.SDK_INT}"
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(INDEX_KEY, 0)
            mIsCheater = savedInstanceState.getBoolean(CHEATER_KEY)
        }
        setContentView(R.layout.activity_quiz)

        loadViews()
        setListeners()
        updateQuestion()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "Saving instance state")
        savedInstanceState.putInt(INDEX_KEY, mCurrentIndex)
        savedInstanceState.putBoolean(CHEATER_KEY, mIsCheater)
    }
    private var mQuestionBank = mutableListOf(
        Question(R.string.question_1, R.string.c_question_1, mutableListOf(R.string.i1_question_1, R.string.c_question_1, R.string.i2_question_1, R.string.i3_question_1)),
        Question(R.string.question_2, R.string.c_question_2, mutableListOf(R.string.c_question_2, R.string.i1_question_2, R.string.i2_question_2, R.string.i3_question_2)),
        Question(R.string.question_3, R.string.c_question_3, mutableListOf(R.string.i1_question_3, R.string.i2_question_3, R.string.i3_question_3, R.string.c_question_3)),
    )

    private fun updateQuestion(){
        val questionId: Int = mQuestionBank[mCurrentIndex].mQuestion
        mQuestionTextView.setText(questionId)
        mAnswer1.setText(getAnswerByButtonPosition(0))
        mAnswer2.setText(getAnswerByButtonPosition(1))
        mAnswer3.setText(getAnswerByButtonPosition(2))
        mAnswer4.setText(getAnswerByButtonPosition(3))
    }

    private fun loadViews(){
        mQuestionTextView = findViewById(R.id.question_text_view)
        mAnswer1 = findViewById(R.id.answer_1_button)
        mAnswer2 = findViewById(R.id.answer_2_button)
        mAnswer3 = findViewById(R.id.answer_3_button)
        mAnswer4 = findViewById(R.id.answer_4_button)
        mNextButton = findViewById(R.id.next_button)
        mPrevButton = findViewById(R.id.prev_button)
        mCheatButton = findViewById(R.id.cheat_button)
    }

    private fun currentCorrectAnswer(): Int{
        return mQuestionBank[mCurrentIndex].mCorrectAnswer
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        val data: Intent = result.data ?: return@registerForActivityResult
        mIsCheater = data.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false)
    }

    private fun setListeners(){
        mNextButton.setOnClickListener{
            mCurrentIndex = (mCurrentIndex+1)%mQuestionBank.size
            mIsCheater = false
            updateQuestion()
        }
        mPrevButton.setOnClickListener{
            mCurrentIndex = ((mCurrentIndex-1)+mQuestionBank.size)%mQuestionBank.size
            mIsCheater = false
            updateQuestion()
        }
        mAnswer1.setOnClickListener {
            checkAnswer(getAnswerByButtonPosition(0))
        }
        mAnswer2.setOnClickListener{
            checkAnswer(getAnswerByButtonPosition(1))
        }
        mAnswer3.setOnClickListener {
            checkAnswer(getAnswerByButtonPosition(2))
        }
        mAnswer4.setOnClickListener {
            checkAnswer(getAnswerByButtonPosition(3))
        }
        mCheatButton.setOnClickListener{
            val payloadIntent: Intent = Intent(this,CheatActivity::class.java).apply {
                putExtra(CheatActivity.EXTRA_ANSWER, currentCorrectAnswer())
            }
            startForResult.launch(payloadIntent)
        }
    }

    private fun getAnswerByButtonPosition(optionPressed: Int): Int{
        return mQuestionBank[mCurrentIndex].mAnswers[optionPressed]
    }

    private fun checkAnswer(answer: Int){
        val correctAnswer = currentCorrectAnswer()
        val toastMessageId: Int = when {
            mIsCheater -> R.string.judgment_toast
            answer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show()
    }


}