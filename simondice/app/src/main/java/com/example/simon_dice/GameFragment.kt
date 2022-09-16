package com.example.simon_dice

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.HapticFeedbackConstants.CLOCK_TICK
import android.view.HapticFeedbackConstants.REJECT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import java.util.*

private const val ARG_MUSIC_ON = "music_on"
private const val ARG_VIBRATE_ON = "vibrate_on"

class GameFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks{
        fun onLostGame(musicOn: Boolean, vibrateOn: Boolean)
    }
    private var callbacks: GameFragment.Callbacks? = null


    private lateinit var mButtonOn: Animation
    private var mButtons: MutableList<Button> = mutableListOf()
    private lateinit var mScoreCounter: TextView
    private var mScore: Int = 0
    private var mSequence: MutableList<Int> = mutableListOf()
    private var mCount: Int = 0
    private lateinit var mVibrator: Vibrator
    private var rnd: Random = Random()

    private var mMusicOn: Boolean = true
    private var mVibrateOn: Boolean = true

    private val mOffColors: List<Int> = listOf(
        Color.parseColor("#40FF0000"),
        Color.parseColor("#4000FF00"),
        Color.parseColor("#400000FF"),
        Color.parseColor("#40FFFF00"))

    private val mOnColors: List<Int> = listOf(
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FF00FF00"),
        Color.parseColor("#FF0000FF"),
        Color.parseColor("#FFFFFF00"))

    private val mWaveforms: List<LongArray> = listOf(
        longArrayOf(0, 50),
        longArrayOf(0, 50, 50, 125),
        longArrayOf(0, 125, 50, 50),
        longArrayOf(0, 125),

    )

    private lateinit var mSounds: List<MediaPlayer>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMusicOn = arguments?.getBoolean(ARG_MUSIC_ON) as Boolean
        mVibrateOn = arguments?.getBoolean(ARG_VIBRATE_ON) as Boolean

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_game, container, false)
        buttonsState(false)

        mScoreCounter = v.findViewById(R.id.score_counter)

        mButtons.add(v.findViewById(R.id.button1))
        mButtons.add(v.findViewById(R.id.button2))
        mButtons.add(v.findViewById(R.id.button3))
        mButtons.add(v.findViewById(R.id.button4))
        for (i in (0..3)){
            mButtons[i].setOnClickListener {
                buttonOn(i)
                if(mSequence[mCount] == i){
                    mCount++
                }else{
                    buttonsState(false)
                    if(mVibrateOn){
                        mVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 100, 250), -1))
                    }
                    callbacks?.onLostGame(mMusicOn, mVibrateOn)
                }
                if(mCount == mSequence.size){
                    buttonsState(false)
                    mScore++
                    mScoreCounter.setText(mScore.toString())
                    Handler(Looper.getMainLooper()).postDelayed({
                        playLevel()
                    }, 1000)
                }
            }
        }
        mButtonOn = AnimationUtils.loadAnimation(context, R.anim.button_on)

        mSounds = listOf(
            MediaPlayer.create(context, R.raw.do_sound),
            MediaPlayer.create(context, R.raw.mi),
            MediaPlayer.create(context, R.raw.sol),
            MediaPlayer.create(context, R.raw.si),
        )

        Handler(Looper.getMainLooper()).postDelayed({
            playLevel()
        }, 1000)

        mVibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        return v
    }

    private fun getOffAnimator(i: Int): ValueAnimator{
        val offAnimator: ValueAnimator = ValueAnimator()
        offAnimator.duration = 0
        offAnimator.setEvaluator(ArgbEvaluator())
        offAnimator.setIntValues(mOnColors[i], mOffColors[i])
        offAnimator.addUpdateListener{
            mButtons[i].setBackgroundColor(it.animatedValue as Int)
        }
        return offAnimator
    }

    private fun getWaitAnimator(i: Int): ValueAnimator{
        val offAnimator: ValueAnimator = getOffAnimator(i)
        val waitAnimator: ValueAnimator = ValueAnimator()
        waitAnimator.duration = 300
        waitAnimator.setEvaluator(ArgbEvaluator())
        waitAnimator.setIntValues(mOnColors[i], mOnColors[i])
        waitAnimator.addUpdateListener{
            mButtons[i].setBackgroundColor(it.animatedValue as Int)
        }

        waitAnimator.doOnEnd {
            offAnimator.start()
        }
        return waitAnimator
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getOnAnimator(i: Int): ValueAnimator{
        val waitAnimator: ValueAnimator = getWaitAnimator(i)
        val onAnimator: ValueAnimator = ValueAnimator()
        onAnimator.duration = 0
        onAnimator.setEvaluator(ArgbEvaluator())
        onAnimator.setIntValues(mOffColors[i], mOnColors[i])
        onAnimator.addUpdateListener{
            mButtons[i].setBackgroundColor(it.animatedValue as Int)
        }

        onAnimator.doOnStart {
            if(mVibrateOn){
                mVibrator.vibrate(VibrationEffect.createWaveform(mWaveforms[i], -1))
            }
            if(mMusicOn){
                mSounds[i].start()
            }
        }

        onAnimator.doOnEnd {
            waitAnimator.start()
        }
        return onAnimator
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getShowAnimator(sequence: List<Int>): List<ValueAnimator>{
        val onAnimators: MutableList<ValueAnimator> = mutableListOf()
        for(e in sequence){
            onAnimators.add(getOnAnimator(e))
        }
        for(i in sequence.indices){
            if(i < sequence.size - 1) {
                onAnimators[i].doOnEnd {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onAnimators[i + 1].start()
                    }, 600)
                }
            }else{
                onAnimators[i].doOnEnd {
                    Handler(Looper.getMainLooper()).postDelayed({
                        buttonsState(true)
                    }, 600)
                }
            }
        }
        return onAnimators
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun buttonOn(i: Int){
        val onAnimator: ValueAnimator = getOnAnimator(i)
        onAnimator.start()

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun playLevel(): Boolean{
        buttonsState(false)
        mSequence.add(rnd.nextInt(4))
        mCount = 0
        Log.i("GameFragment", "Secuencia: $mSequence")
        val showAnimator: List<ValueAnimator> = getShowAnimator(mSequence)
        showAnimator[0].start()
        return true
    }

    private fun buttonsState(state: Boolean){
        for (button in mButtons){
            button.isEnabled = state
        }
    }

    companion object{
        fun newInstance(musicOn: Boolean, vibrateOn: Boolean): GameFragment{
            val args: Bundle = Bundle().apply {
                putBoolean(ARG_MUSIC_ON, musicOn)
                putBoolean(ARG_VIBRATE_ON, vibrateOn)
            }
            return GameFragment().apply {
                arguments = args
            }
        }
    }
}