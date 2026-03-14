package com.laviesss.wsanotihub.ui

import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.laviesss.wsanotihub.databinding.ItemNotificationBinding
import com.laviesss.wsanotihub.databinding.ItemNotificationGroupHeaderBinding
import com.laviesss.wsanotihub.data.NotificationEntity
import com.laviesss.wsanotihub.model.NotificationGroup
import java.util.regex.Pattern

class GroupedNotificationAdapter(
    private val onHeaderClick: (NotificationGroup) -> Unit,
    private val onReply: (NotificationEntity, String) -> Unit,
    private val onOpen: (NotificationEntity) -> Unit,
    private val onDismiss: (NotificationEntity) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is NotificationGroup) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemNotificationGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ItemViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is HeaderViewHolder && item is NotificationGroup) {
            holder.bind(item)
        } else if (holder is ItemViewHolder && item is NotificationEntity) {
            holder.bind(item)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemNotificationGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: NotificationGroup) {
            binding.appLabel.text = group.appLabel
            binding.unreadBadge.text = group.unreadCount.toString()
            binding.unreadBadge.visibility = if (group.unreadCount > 0) View.VISIBLE else View.GONE
            binding.mostRecentPreview.text = "${group.mostRecent.title}: ${group.mostRecent.body}"
            binding.timestamp.text = DateUtils.getRelativeTimeSpanString(group.mostRecent.timestamp)

            try {
                val icon = binding.root.context.packageManager.getApplicationIcon(group.appPackageName)
                Glide.with(binding.appIcon).load(icon).into(binding.appIcon)
            } catch (e: Exception) {
                binding.appIcon.setImageDrawable(null)
            }

            binding.expandChevron.rotation = if (group.isExpanded) 180f else 0f
            binding.root.setOnClickListener { onHeaderClick(group) }
        }
    }

    inner class ItemViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: NotificationEntity) {
            binding.notiTitle.text = entity.title
            binding.notiBody.text = entity.body
            binding.timestamp.text = DateUtils.getRelativeTimeSpanString(entity.timestamp)

            // OTP Detection
            val otpPattern = Pattern.compile("\\b\\d{4,8}\\b")
            val matcher = otpPattern.matcher(entity.body)
            if (matcher.find()) {
                val otp = matcher.group()
                binding.btnCopyCode.visibility = View.VISIBLE
                binding.btnCopyCode.setOnClickListener {
                    if (!com.laviesss.wsanotihub.billing.ProManager.isPro()) {
                        (binding.root.context as? androidx.appcompat.app.AppCompatActivity)?.let { activity ->
                            ProUpgradeDialog().show(activity.supportFragmentManager, "pro_upgrade")
                        }
                        return@setOnClickListener
                    }
                    val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("OTP", otp)
                    clipboard.setPrimaryClip(clip)
                }
            } else {
                binding.btnCopyCode.visibility = View.GONE
            }

            binding.btnReply.visibility = if (entity.hasReplyAction) View.VISIBLE else View.GONE
            binding.btnReply.setOnClickListener {
                binding.replyContainer.visibility = if (binding.replyContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            binding.btnSendReply.setOnClickListener {
                val replyText = binding.etReply.text.toString()
                if (replyText.isNotEmpty()) {
                    onReply(entity, replyText)
                    binding.replyContainer.visibility = View.GONE
                    binding.etReply.text.clear()
                }
            }

            binding.btnOpen.setOnClickListener { onOpen(entity) }
            binding.btnDismiss.setOnClickListener { onDismiss(entity) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is NotificationGroup && newItem is NotificationGroup) {
                oldItem.appPackageName == newItem.appPackageName
            } else if (oldItem is NotificationEntity && newItem is NotificationEntity) {
                oldItem.id == newItem.id
            } else false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
