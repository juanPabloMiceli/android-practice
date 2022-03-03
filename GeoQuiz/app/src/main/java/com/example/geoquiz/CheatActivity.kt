package com.example.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

private const val EXTRA_ANSWER: String = "com.example.geoquiz.answer"

class CheatActivity: Activity() {

    private lateinit var mAnswerTextView: TextView
    private lateinit var mShowAnswerButton: Button
    private var mAnswerShown: Boolean = false
    private val CHEATER_KEY: String = "cheaterKey"


    companion object{
        fun newIntent(packageContext: Context, answer: Int): Intent{
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER, answer)
            }
        }
        val EXTRA_ANSWER_SHOWN: String = "com.example.geoquiz.answer_shown"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        loadViews()
        setListeners()

        if(savedInstanceState != null){
            if(savedInstanceState.getBoolean(CHEATER_KEY)){
                showAnswer()
            }
        }
        setAnswerShownResult(mAnswerShown)

    }

    private fun showAnswer(){
        val answer = intent.getIntExtra(EXTRA_ANSWER, 0)
        mAnswerShown = true
        setAnswerShownResult(mAnswerShown)
        mAnswerTextView.setText(answer)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean(CHEATER_KEY, mAnswerShown)


    }

    private fun loadViews(){
        mAnswerTextView = findViewById(R.id.answerTextView)
        mShowAnswerButton = findViewById(R.id.showAnswerButton)
    }

    private fun setListeners(){

        mShowAnswerButton.setOnClickListener {
            showAnswer()
        }
    }

    private fun setAnswerShownResult(isAnswerShown: Boolean) {
        val data: Intent = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown)
        }
        setResult(RESULT_OK, data)
    }

}