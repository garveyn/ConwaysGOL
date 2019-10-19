package com.nick.conwaygameoflife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class SingleFragmentActivity : AppCompatActivity() {

    abstract fun createFragment() : Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fm = supportFragmentManager
        var fragment = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    fun replaceFragment(newFragment: Fragment) {
        val fm = supportFragmentManager
        val oldFragment = fm.findFragmentById(R.id.fragment_container)

        if (oldFragment != null) {
            fm.beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(newFragment.toString())
                .commit()

        } else {
            fm.beginTransaction()
                .add(R.id.fragment_container, newFragment)
                .commit()
        }

    }
}
