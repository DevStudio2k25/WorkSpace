package com.devstudio.workspace.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object NotesList : Screen("notes_list")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: Long? = null) = if (noteId != null) "note_editor/$noteId" else "note_editor/new"
    }
    object PinLock : Screen("pin_lock")
    object Vault : Screen("vault")
    object VaultItemEditor : Screen("vault_item_editor/{itemId}") {
        fun createRoute(itemId: Long? = null) = if (itemId != null) "vault_item_editor/$itemId" else "vault_item_editor/new"
    }
    object Settings : Screen("settings")
    object VaultSettings : Screen("vault_settings")
    object VaultImageViewer : Screen("vault_image_viewer/{itemId}") {
        fun createRoute(itemId: Long) = "vault_image_viewer/$itemId"
    }
    object VaultVideoPlayer : Screen("vault_video_player/{itemId}") {
        fun createRoute(itemId: Long) = "vault_video_player/$itemId"
    }
    object VaultAudioPlayer : Screen("vault_audio_player/{itemId}") {
        fun createRoute(itemId: Long) = "vault_audio_player/$itemId"
    }
}
