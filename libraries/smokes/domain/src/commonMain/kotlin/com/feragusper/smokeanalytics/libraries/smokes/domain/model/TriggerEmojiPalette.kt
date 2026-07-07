package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * Curated emoji options for the trigger icon picker (Settings → Manage triggers).
 * Shared by mobile and web so both offer the same choices. Order matters: the
 * built-in defaults come first, then common smoking-context emoji.
 */
val TriggerEmojiPalette: List<String> = listOf(
    // Built-in defaults
    "☕", "🍺", "🥱", "😰", "😖", "🍽️", "👥", "⏸️", "🚗", "📱",
    // Drinks & food
    "🍷", "🥃", "🧉", "🍔", "🍕", "🍰", "🫖",
    // Moods
    "😤", "😢", "😴", "🤯", "😡", "🥳", "😌", "🫠",
    // Activities & places
    "💼", "💻", "📺", "🎮", "🎵", "🏃", "🚶", "🛏️", "🚿", "🚌",
    "🌧️", "☀️", "🌙", "⏰", "🎉", "🏠", "🏢", "📚", "☎️", "🧠",
)
