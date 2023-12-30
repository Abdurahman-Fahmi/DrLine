package com.wecareapp.android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wecareapp.android.R
import com.wecareapp.android.adapter.Custom2Adapter
import com.wecareapp.android.ui.activity.ChatActivity
import kotlinx.android.synthetic.main.activity_chat_patient.*
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.recyclerView

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientChatFragment(private val listener: ICallBackToActivity) : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.activity_chat_patient, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = Custom2Adapter(dummyList, R.layout.list_item_notification)
            recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
                ): Boolean {
                    openChat()
                    return false
                }

                override fun onTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
                ) {
                }

                override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openChat() {
        activity?.startActivity(Intent(context, ChatActivity::class.java))
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}