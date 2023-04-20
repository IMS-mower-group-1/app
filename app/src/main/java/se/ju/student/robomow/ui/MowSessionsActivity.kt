package se.ju.student.robomow.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import se.ju.student.robomow.adapter.MowSessionAdapter
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.viewmodel.MowSessionsViewModel

@AndroidEntryPoint
class MowSessionsActivity : AppCompatActivity() {
    private lateinit var mowSessionsAdapter: MowSessionAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mow_sessions)
        initViewModel()
        initRecyclerView()
    }

    private fun initViewModel() {
        val mowSessionsViewModel = ViewModelProvider(this)[MowSessionsViewModel::class.java]
        mowSessionsViewModel.mowSessions.observe(this) { mowSessions ->
            mowSessionsAdapter.updateData(mowSessions)
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        mowSessionsAdapter = MowSessionAdapter(listOf()) { mowSession ->
            Intent(this, MapActivity::class.java).also { intent ->
                intent.putExtra("MOW_SESSION", mowSession)
                startActivity(intent)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mowSessionsAdapter
    }
}