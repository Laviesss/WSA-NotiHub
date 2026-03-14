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
import com.laviesss.wsanotihub.data.NotificationEntity
import java.util.regex.Pattern

class NotificationAdapter(
    private val onReply: (NotificationEntity, String) -> Unit,
    private val onOpen: (NotificationEntity) -> Unit,
    private val onDismiss: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback) {

    private var isWindowsFriendlyMode = false

    fun setWindowsFriendlyMode(enabled: Boolean) {
        isWindowsFriendlyMode = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: NotificationEntity) {
            binding.appLabel.text = entity.appLabel
            binding.notiTitle.text = entity.title
            binding.notiBody.text = entity.body
            binding.timestamp.text = DateUtils.getRelativeTimeSpanString(entity.timestamp)

            try {
                val icon = binding.root.context.packageManager.getApplicationIcon(entity.appPackageName)
                Glide.with(binding.appIcon).load(icon).into(binding.appIcon)
            } catch (e: Exception) {
                binding.appIcon.setImageDrawable(null)
            }

            binding.btnReply.visibility = if (entity.hasReplyAction) View.VISIBLE else View.GONE

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

            if (isWindowsFriendlyMode) {
                val minHeight = (56 * binding.root.context.resources.displayMetrics.density).toInt()
                binding.btnReply.minHeight = minHeight
                binding.btnOpen.minHeight = minHeight
                binding.btnDismiss.minHeight = minHeight
                binding.btnCopyCode.minHeight = minHeight
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity) = oldItem == newItem
    }
}
