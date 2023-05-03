package se.ju.student.robomow.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import se.ju.student.robomow.adapter.MowSessionAdapter
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.view.utils.ItemDividerDecoration
import se.ju.student.robomow.ui.viewmodel.MowSessionsViewModel

@AndroidEntryPoint
class MowSessionsActivity : AppCompatActivity() {
    private lateinit var mowSessionsAdapter: MowSessionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mowSessionsViewModel: MowSessionsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mow_sessions)
        initViewModel()
        initRecyclerView()

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        mowSessionsViewModel.isLoading.observe(this) {
            swipeRefreshLayout.isRefreshing = it
        }
        swipeRefreshLayout.setOnRefreshListener {
            mowSessionsViewModel.getMowSessions()
        }
    }

    private fun initViewModel() {
        mowSessionsViewModel = ViewModelProvider(this)[MowSessionsViewModel::class.java]
        mowSessionsViewModel.mowSessions.observe(this) { mowSessions ->
            mowSessionsAdapter.updateData(mowSessions)
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        mowSessionsAdapter = MowSessionAdapter(this, listOf()) { mowSession ->
            Intent(this, MapActivity::class.java).also { intent ->
                intent.putExtra("MOW_SESSION", mowSession)
                startActivity(intent)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mowSessionsAdapter
        recyclerView.addItemDecoration(ItemDividerDecoration(5))
    }
}