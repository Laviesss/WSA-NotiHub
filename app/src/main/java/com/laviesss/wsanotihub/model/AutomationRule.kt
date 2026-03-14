package com.laviesss.wsanotihub.model

enum class ActionType {
    AUTO_COPY_OTP,
    AUTO_DISMISS,
    OPEN_APP
}

data class AutomationRule(
    val triggerPackage: String, // "*" for any app
    val actionType: ActionType,
    val isEnabled: Boolean = true
)
