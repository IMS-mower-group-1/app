package se.ju.student.robomow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import se.ju.student.robomow.R
import se.ju.student.robomow.model.MowSession
import se.ju.student.robomow.ui.viewmodel.MowSessionsViewModel

@AndroidEntryPoint
class MowSessionsActivity : AppCompatActivity() {
    private lateinit var mowSessionsAdapter: ArrayAdapter<MowSession>
    private lateinit var mowSessionsListView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mow_sessions)
        initViewModel()
        initListView()

        mowSessionsListView.setOnItemClickListener { parent, view, position, id ->
            println(mowSessionsListView.getItemAtPosition((position)))
            val mowSession = mowSessionsListView.getItemAtPosition(position)
            if (mowSession is MowSession) {
                Intent(this, TestParcelableActivity::class.java).also {intent ->
                    intent.putExtra("MOW_SESSION", mowSession)
                    startActivity(intent)
                }
            }
        }

    }

    private fun initViewModel() {
        val mowSessionsViewModel = ViewModelProvider(this)[MowSessionsViewModel::class.java]
        mowSessionsViewModel.mowSessions.observe(this) { mowSessions ->
            mowSessionsAdapter.clear()
            mowSessionsAdapter.addAll(mowSessions)
        }
    }
    private fun initListView() {
        mowSessionsListView = findViewById(R.id.mow_sessions_list_view)
        mowSessionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            ArrayList<MowSession>()
        )
        mowSessionsListView.adapter = mowSessionsAdapter
    }
}