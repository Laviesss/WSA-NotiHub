package com.laviesss.wsanotihub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.laviesss.wsanotihub.databinding.FragmentDashboardBinding
import com.laviesss.wsanotihub.service.NotiHubListenerService
import com.laviesss.wsanotihub.util.NotificationReplyHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: GroupedNotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupSortSpinner()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupedNotifications.collectLatest { groups ->
                val displayList = mutableListOf<Any>()
                groups.forEach { group ->
                    displayList.add(group)
                    if (group.isExpanded) {
                        displayList.addAll(group.notifications)
                    }
                }
                adapter.submitList(displayList)

                binding.emptyState.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = GroupedNotificationAdapter(
            onHeaderClick = { group -> viewModel.toggleGroup(group.appPackageName) },
            onReply = { entity, text ->
                val sbn = NotiHubListenerService.getActiveSbn(entity.notificationKey)
                if (sbn != null) {
                    NotificationReplyHelper.sendReply(requireContext(), sbn, text)
                }
            },
            onOpen = { entity ->
                val sbn = NotiHubListenerService.getActiveSbn(entity.notificationKey)
                sbn?.notification?.contentIntent?.send()
            },
            onDismiss = { entity -> viewModel.dismissNotification(entity.id) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupSortSpinner() {
        val options = arrayOf("Newest", "By App", "By Priority")
        binding.sortSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSortOrder(options[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
