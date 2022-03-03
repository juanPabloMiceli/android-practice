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
import androidx.lifecycle.ViewModelProvider

private const val TAG: String = "QuizActivity"
private const val INDEX_KEY: String = "indexKey"
private const val CHEATER_KEY: String = "cheaterKey"

class QuizActivity : AppCompatActivity() {

    private lateinit var mAnswer1: Button
    private lateinit var mAnswer2: Button
    private lateinit var mAnswer3: Button
    private lateinit var mAnswer4: Button
    private lateinit var mQuestionTextView: TextView
    private lateinit var mNextButton: ImageButton
    private lateinit var mPrevButton: ImageButton
    private lateinit var mCheatButton: Button

    private val geoQuizViewModel: GeoQuizViewModel by lazy {
        ViewModelProvider(this).get(GeoQuizViewModel::class.java)
    }

    override fun onDestroy(){
        super.onDestroy()
        Log.d(TAG, "Goodbye cruel world :(")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Creating activity")
        supportActionBar?.subtitle = "API level ${Build.VERSION.SDK_INT}"

        geoQuizViewModel.mCurrentIndex = savedInstanceState?.getInt(INDEX_KEY) ?: 0
        geoQuizViewModel.mIsCheater = savedInstanceState?.getBoolean(CHEATER_KEY) ?: false
        setContentView(R.layout.activity_quiz)

        loadViews()
        setListeners()
        updateQuestion()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "Saving instance state")
        savedInstanceState.putInt(INDEX_KEY, geoQuizViewModel.mCurrentIndex)
        savedInstanceState.putBoolean(CHEATER_KEY, geoQuizViewModel.mIsCheater)
    }

    private fun updateQuestion(){
        val questionId: Int = geoQuizViewModel.currentQuestionText
        mQuestionTextView.setText(questionId)
        mAnswer1.setText(geoQuizViewModel.answerAtButtonPosition(0))
        mAnswer2.setText(geoQuizViewModel.answerAtButtonPosition(1))
        mAnswer3.setText(geoQuizViewModel.answerAtButtonPosition(2))
        mAnswer4.setText(geoQuizViewModel.answerAtButtonPosition(3))
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

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        geoQuizViewModel.mIsCheater = result.data?.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false) ?: false
    }

    private fun setListeners(){
        mNextButton.setOnClickListener{
            geoQuizViewModel.moveToNext()
            geoQuizViewModel.mIsCheater = false
            updateQuestion()
        }
        mPrevButton.setOnClickListener{
            geoQuizViewModel.moveToPrevious()
            geoQuizViewModel.mIsCheater = false
            updateQuestion()
        }
        mAnswer1.setOnClickListener {
            checkAnswer(geoQuizViewModel.answerAtButtonPosition(0))
        }
        mAnswer2.setOnClickListener{
            checkAnswer(geoQuizViewModel.answerAtButtonPosition(1))
        }
        mAnswer3.setOnClickListener {
            checkAnswer(geoQuizViewModel.answerAtButtonPosition(2))
        }
        mAnswer4.setOnClickListener {
            checkAnswer(geoQuizViewModel.answerAtButtonPosition(3))
        }
        mCheatButton.setOnClickListener{
            val payloadIntent: Intent = CheatActivity.newIntent(this@QuizActivity, geoQuizViewModel.currentQuestionAnswer)
            startForResult.launch(payloadIntent)
        }
    }

    private fun checkAnswer(answer: Int){
        val correctAnswer = geoQuizViewModel.currentQuestionAnswer
        val toastMessageId: Int = when {
            geoQuizViewModel.mIsCheater -> R.string.judgment_toast
            answer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this@QuizActivity, toastMessageId, Toast.LENGTH_SHORT).show()
    }


}