package com.nick.conwaygameoflife

import androidx.fragment.app.Fragment

class MainActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return ConwayFragment()
    }

}