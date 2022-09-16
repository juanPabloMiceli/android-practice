package com.example.simon_dice

import android.content.Context
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.polyak.iconswitch.IconSwitch

private const val ARG_MUSIC_ON = "music_on"
private const val ARG_VIBRATE_ON = "vibrate_on"

class StartGameFragment : Fragment(){


    /**
     * Required interface for hosting activities
     */
    interface Callbacks{
        fun onStartGame(musicOn: Boolean, vibrateOn: Boolean)
    }

    private lateinit var mStartGameButton: Button
    private lateinit var mVibrateSwitch: IconSwitch
    private lateinit var mMusicSwitch: IconSwitch
    private var mMusicOn: Boolean = true
    private var mVibrateOn: Boolean = true


    private var callbacks: Callbacks? = null



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_start, container, false)
        val switchVibrator: Vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val switchSound: MediaPlayer = MediaPlayer.create(context, R.raw.do_sound)

        mMusicOn = arguments?.getBoolean(ARG_MUSIC_ON) as Boolean
        mVibrateOn = arguments?.getBoolean(ARG_VIBRATE_ON) as Boolean
        mStartGameButton = v.findViewById(R.id.start_game_button)
        mStartGameButton.setOnClickListener { startGame() }

        mVibrateSwitch = v.findViewById(R.id.vibrate_switch)
        mVibrateSwitch.setCheckedChangeListener {
            when(it){
                IconSwitch.Checked.LEFT -> mVibrateOn = false
                IconSwitch.Checked.RIGHT -> {
                    mVibrateOn = true
                    switchVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100), -1))
                }
            }
        }

        mMusicSwitch = v.findViewById(R.id.music_switch)
        mMusicSwitch.setCheckedChangeListener {
            when(it){
                IconSwitch.Checked.LEFT -> mMusicOn = false
                IconSwitch.Checked.RIGHT -> {
                    mMusicOn = true
                    switchSound.start()
                }
            }
        }
        when(mMusicOn){
            true -> mMusicSwitch.checked = IconSwitch.Checked.RIGHT
            false -> mMusicSwitch.checked = IconSwitch.Checked.LEFT
        }
        when(mVibrateOn){
            true -> mVibrateSwitch.checked = IconSwitch.Checked.RIGHT
            false -> mVibrateSwitch.checked = IconSwitch.Checked.LEFT
        }
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun startGame(){
        callbacks?.onStartGame(mMusicOn, mVibrateOn)
    }

    companion object{
        fun newInstance(musicOn: Boolean, vibrateOn: Boolean): StartGameFragment{
            val args: Bundle = Bundle().apply {
                putBoolean(ARG_MUSIC_ON, musicOn)
                putBoolean(ARG_VIBRATE_ON, vibrateOn)
            }
            return StartGameFragment().apply {
                arguments = args
            }
        }
    }
}