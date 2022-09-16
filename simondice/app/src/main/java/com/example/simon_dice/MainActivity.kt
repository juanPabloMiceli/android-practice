package com.example.simon_dice

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.Fragment
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class MainActivity : AppCompatActivity(),
    StartGameFragment.Callbacks, GameFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.default_frame)

        if(fragment == null){
            fragment = StartGameFragment.newInstance(true, true)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.default_frame, fragment)
                .commit()
        }
    }

    override fun onStartGame(musicOn: Boolean, vibrateOn: Boolean) {
        val fragment: Fragment = GameFragment.newInstance(musicOn, vibrateOn)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.default_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onLostGame(musicOn: Boolean, vibrateOn: Boolean) {
        val fragment: Fragment = StartGameFragment.newInstance(musicOn, vibrateOn)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.default_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    //    private fun startLevel(){
//        val sequence  = IntArray(mLevel) { Random().nextInt(4) }.asList()
//        for (i in sequence) {
//            Log.i("TAG", "Aca con i = $i")
//            mButtons.elementAt(i).setBackgroundColor(Color.GREEN)
//            Thread.sleep(2000)
//            mButtons.elementAt(i).setBackgroundColor(Color.RED)
//        }
//        mLevel++
//    }
//
//    private fun loadViews(){
//        mButtons.add(findViewById(R.id.button1))
//        mButtons.add(findViewById(R.id.button2))
//        mButtons.add(findViewById(R.id.button3))
//        mButtons.add(findViewById(R.id.button4))
//        mStartButton = findViewById(R.id.start_button)
//    }

}