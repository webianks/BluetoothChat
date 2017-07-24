package com.webianks.bluechat

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by ramankit on 24/7/17.
 */

class ChatFragment : Fragment() {

    companion object {

        fun newInstance(): ChatFragment {
            val myFragment = ChatFragment()
            val args = Bundle()
            myFragment.arguments = args
            return myFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val mView: View  = LayoutInflater.from(activity).inflate(R.layout.chat_fragment, container, false)
        initViews(mView)
        return mView
    }

    private fun initViews(mView: View) {

    }


}