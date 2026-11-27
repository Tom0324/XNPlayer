package app.marlboroadvance.mpvex.ui.browser.networkstreaming

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.marlboroadvance.mpvex.database.dao.NetworkConnectionDao
import app.marlboroadvance.mpvex.domain.network.NetworkConnection
import app.marlboroadvance.mpvex.domain.network.NetworkFile
import app.marlboroadvance.mpvex.preferences.preference.collectAsState
import app.marlboroadvance.mpvex.presentation.Screen
import app.marlboroadvance.mpvex.presentation.components.pullrefresh.PullRefreshBox
import app.marlboroadvance.mpvex.ui.browser.cards.NetworkFolderCard
import app.marlboroadvance.mpvex.ui.browser.cards.NetworkVideoCard
import app.marlboroadvance.mpvex.ui.browser.components.BrowserTopBar
import app.marlboroadvance.mpvex.preferences.BrowserPreferences
import app.marlboroadvance.mpvex.preferences.preference.collectAsState
import app.marlboroadvance.mpvex.ui.browser.states.EmptyState
import app.marlboroadvance.mpvex.ui.preferences.PreferencesScreen
import app.marlboroadvance.mpvex.ui.utils.LocalBackStack
import app.marlboroadvance.mpvex.utils.history.NetworkRecentUri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Serializable
data class NetworkBrowserScreen(
  val connectionId: Long,
  val connectionName: String,
  val currentPath: String = "/",
) : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current

    val viewModel: NetworkBrowserViewModel =
      viewModel(
        key = "NetworkBrowser_${connectionId}_$currentPath",
        factory =
          NetworkBrowserViewModel.factory(
            context.applicationContext as android.app.Application,
            connectionId,
            currentPath,
          ),
      )

    val files by viewModel.files.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val recentlyPlayedFilePaths by viewModel.recentlyPlayedFilePaths.collectAsState()
    val browserPreferences = org.koin.compose.koinInject<BrowserPreferences>()
    val autoScrollToLastPlayed by browserPreferences.autoScrollToLastPlayed.collectAsState()

    // UI State
    val isRefreshing = remember { mutableStateOf(false) }

    // Load files when connectionId or currentPath changes
    LaunchedEffect(connectionId, currentPath) {
      viewModel.loadFiles()
    }

    BackHandler {
      backstack.removeLastOrNull()
    }

    Scaffold(
      topBar = {
        BrowserTopBar(
          title = connectionName,
          isInSelectionMode = false,
          selectedCount = 0,
          totalCount = 0,
          onBackClick = { backstack.removeLastOrNull() },
          onCancelSelection = {},
          onSortClick = null,
          onSearchClick = null,
          onSettingsClick = {
            backstack.add(app.marlboroadvance.mpvex.ui.preferences.PreferencesScreen)
          },
          onDeleteClick = null,
          onRenameClick = null,
          isSingleSelection = false,
          onInfoClick = null,
          onShareClick = null,
          onPlayClick = null,
          onSelectAll = null,
          onInvertSelection = null,
          onDeselectAll = null,
        )
      },
    ) { padding ->
      NetworkBrowserContent(
        files = files,
        connectionId = connectionId,
        connectionName = connectionName,
        currentPath = currentPath,
        recentlyPlayedFilePaths = recentlyPlayedFilePaths,
        autoScrollToLastPlayed = autoScrollToLastPlayed,
        isLoading = isLoading && files.isEmpty(),
        isRefreshing = isRefreshing,
        error = error,
        onRefresh = { viewModel.loadFiles() },
        onFolderClick = { folder ->
          backstack.add(
            NetworkBrowserScreen(
              connectionId = connectionId,
              connectionName = connectionName,
              currentPath = folder.path,
            ),
          )
        },
        onVideoClick = { video, videos ->
          viewModel.playVideo(video, videos)
        },
        modifier = Modifier.padding(padding),
      )
    }
  }
}

@Composable
private fun NetworkBrowserContent(
  files: List<NetworkFile>,
  connectionId: Long,
  connectionName: String,
  currentPath: String,
  recentlyPlayedFilePaths: List<String>,
  autoScrollToLastPlayed: Boolean,
  isLoading: Boolean,
  isRefreshing: MutableState<Boolean>,
  error: String?,
  onRefresh: suspend () -> Unit,
  onFolderClick: (NetworkFile) -> Unit,
  onVideoClick: (NetworkFile, List<NetworkFile>) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Load connection details
  val dao = org.koin.compose.koinInject<NetworkConnectionDao>()
  var connection by remember { mutableStateOf<NetworkConnection?>(null) }

  LaunchedEffect(connectionId) {
    connection = dao.getConnectionById(connectionId)
  }

  when {
    isLoading -> {
      Box(
        modifier = modifier
          .fillMaxSize()
          .padding(bottom = 80.dp), // Account for bottom navigation bar
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(48.dp),
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }

    error != null -> {
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        EmptyState(
          icon = Icons.Filled.Folder,
          title = "Error loading files",
          message = error,
        )
      }
    }

    files.isEmpty() -> {
      Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        EmptyState(
          icon = Icons.Filled.Folder,
          title = "Empty folder",
          message = "This folder contains no files or directories",
        )
      }
    }

    else -> {
      val folders = files.filter { it.isDirectory }
      val videos = files.filter { !it.isDirectory && it.mimeType?.startsWith("video/") == true }

      val lastPlayedPath = remember(recentlyPlayedFilePaths, videos) {
        recentlyPlayedFilePaths.asSequence()
          .mapNotNull { NetworkRecentUri.decode(it)?.path }
          .firstOrNull { recentPath ->
            // The current folder is the source of truth. If a recent entry has
            // the same path or file name as a video rendered here, it is the
            // correct scroll target even when the stored parent path differs
            // by encoding or a leading slash.
            val exactMatch = videos.any { it.path.trim('/').equals(recentPath.trim('/'), ignoreCase = true) }
            val fileName = recentPath.substringAfterLast('/')
            val nameMatch = videos.any { it.name.equals(fileName, ignoreCase = true) }
            exactMatch || nameMatch
          }
      }

      LaunchedEffect(recentlyPlayedFilePaths, currentPath, lastPlayedPath) {
        Log.d(
          "NetworkAutoScroll",
          "currentPath=$currentPath recentCount=${recentlyPlayedFilePaths.size} " +
            "videoCount=${videos.size} enabled=$autoScrollToLastPlayed lastPlayedPath=$lastPlayedPath",
        )
      }

      val targetVideoIndex = if (autoScrollToLastPlayed && lastPlayedPath != null) {
        videos.indexOfFirst { video ->
          val recentName = lastPlayedPath.substringAfterLast('/')
          video.path.trim('/').equals(lastPlayedPath.trim('/'), ignoreCase = true) ||
            video.name.equals(recentName, ignoreCase = true)
        }
      } else {
        -1
      }
      val targetListIndex = if (targetVideoIndex >= 0) {
        (if (folders.isNotEmpty()) 1 else 0) + folders.size +
          (if (videos.isNotEmpty()) 1 else 0) + targetVideoIndex
      } else {
        0
      }

      val networkListState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (autoScrollToLastPlayed) targetListIndex else 0,
      )

      // The list is composed with the target index as its initial scroll position, but
      // two cases still leave the screen stuck at the top:
      //  1) coming back from Settings/player reuses this composition and only the
      //     `rememberLazyListState` remembers an already-created state at index 0;
      //  2) `scrollToItem` runs before LazyColumn has measured any item, so it is a no-op.
      // Re-apply the scroll whenever the target or the visible item count changes and
      // wait until the list is actually laid out before scrolling.
      LaunchedEffect(targetListIndex, autoScrollToLastPlayed, videos.size, folders.size) {
        if (autoScrollToLastPlayed && targetListIndex > 0) {
          androidx.compose.runtime.snapshotFlow { networkListState.layoutInfo.totalItemsCount }
            .first { count -> count > targetListIndex }
          networkListState.scrollToItem(targetListIndex)
          Log.d(
            "NetworkAutoScroll",
            "scrolled targetListIndex=$targetListIndex firstVisibleItem=${networkListState.firstVisibleItemIndex}",
          )
        }
      }

      // Check if at top of list to hide scrollbar during pull-to-refresh
      val isAtTop by remember {
        derivedStateOf {
          networkListState.firstVisibleItemIndex == 0 && networkListState.firstVisibleItemScrollOffset == 0
        }
      }

      // Only show scrollbar if list has more than 20 items (folders + videos)
      val hasEnoughItems = (folders.size + videos.size) > 20

      // Animate scrollbar alpha
      val scrollbarAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isAtTop || !hasEnoughItems) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "scrollbarAlpha",
      )

      PullRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        listState = networkListState,
        modifier = modifier.fillMaxSize(),
      ) {
        val navigationBarHeight = app.marlboroadvance.mpvex.ui.browser.LocalNavigationBarHeight.current
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navigationBarHeight)
        ) {
          LazyColumnScrollbar(
            state = networkListState,
            settings = ScrollbarSettings(
              thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scrollbarAlpha),
              thumbSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = scrollbarAlpha),
            ),
          ) {
            LazyColumn(
              state = networkListState,
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = navigationBarHeight
              ),
            ) {
            // Folders section
            if (folders.isNotEmpty()) {
              item {
                Text(
                  text = "文件夹",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                )
              }
              items(
                items = folders,
                key = { it.path },
              ) { folder ->
                NetworkFolderCard(
                  file = folder,
                  onClick = { onFolderClick(folder) },
                  modifier = Modifier,
                )
              }
            }

            // Videos section
            if (videos.isNotEmpty()) {
              item {
                Text(
                  text = "Videos",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                )
              }
              items(
                items = videos,
                key = { it.path },
              ) { video ->
                // Only show card if connection is loaded
                connection?.let { conn ->
                  NetworkVideoCard(
                    file = video,
                    connection = conn,
                    onClick = { onVideoClick(video, videos) },
                    modifier = Modifier,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
}
