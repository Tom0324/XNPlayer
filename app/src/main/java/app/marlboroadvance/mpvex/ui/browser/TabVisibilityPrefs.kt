package app.marlboroadvance.mpvex.ui.browser

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TabVisibilityPrefs {
  private const val PREFS = "xnplayer_tab_visibility"
  const val KEY_HOME = "show_home_tab"
  const val KEY_RECENT = "show_recent_tab"
  const val KEY_PLAYLIST = "show_playlist_tab"
  const val KEY_NETWORK = "show_network_tab"
  const val KEY_DEFAULT = "default_tab"

  data class State(
    val showHome: Boolean = true,
    val showRecent: Boolean = true,
    val showPlaylist: Boolean = true,
    val showNetwork: Boolean = true,
    val defaultTab: Int = 0,
  )

  private var loaded = false
  var state by mutableStateOf(State())
    private set

  fun visibleTabs(s: State): List<Int> = buildList {
    if (s.showHome) add(0)
    if (s.showRecent) add(1)
    if (s.showPlaylist) add(2)
    if (s.showNetwork) add(3)
  }

  fun sanitize(input: State): State {
    var s = input.copy(defaultTab = input.defaultTab.coerceIn(0, 3))
    if (visibleTabs(s).isEmpty()) s = s.copy(showHome = true)
    if (s.defaultTab !in visibleTabs(s)) s = s.copy(defaultTab = visibleTabs(s).first())
    return s
  }

  fun load(context: Context): State {
    if (!loaded) {
      val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      state = sanitize(
        State(
          showHome = p.getBoolean(KEY_HOME, true),
          showRecent = p.getBoolean(KEY_RECENT, true),
          showPlaylist = p.getBoolean(KEY_PLAYLIST, true),
          showNetwork = p.getBoolean(KEY_NETWORK, true),
          defaultTab = p.getInt(KEY_DEFAULT, 0),
        ),
      )
      loaded = true
    }
    return state
  }

  fun write(context: Context, input: State) {
    val s = sanitize(input)
    state = s
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
      .putBoolean(KEY_HOME, s.showHome)
      .putBoolean(KEY_RECENT, s.showRecent)
      .putBoolean(KEY_PLAYLIST, s.showPlaylist)
      .putBoolean(KEY_NETWORK, s.showNetwork)
      .putInt(KEY_DEFAULT, s.defaultTab)
      .apply()
  }
}
