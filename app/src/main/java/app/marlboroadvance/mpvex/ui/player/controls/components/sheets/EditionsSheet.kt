package app.marlboroadvance.mpvex.ui.player.controls.components.sheets

import android.text.format.DateUtils
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.marlboroadvance.mpvex.R
import app.marlboroadvance.mpvex.ui.player.EditionNode
import app.marlboroadvance.mpvex.ui.theme.spacing
import `is`.xyz.mpv.MPVLib
import kotlinx.collections.immutable.ImmutableList

/**
 * Disc title selector for ISO/DVD/Blu-ray playback. mpv 0.42 exposes disc
 * titles as "editions". Tapping a row switches to that title; the synthetic
 * "Disc Menu" command is offered for DVD discs.
 */
@Composable
fun EditionsSheet(
  editions: ImmutableList<EditionNode>,
  currentEdition: Int?,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  GenericTracksSheet(
    tracks = editions,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    header = {
      Text(
        text = stringResource(R.string.player_sheets_editions_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.smaller),
      )
    },
    track = { edition ->
      val details =
        edition.length?.takeIf { it > 0 }?.let { DateUtils.formatElapsedTime(it.toLong()) }
      AudioTrackRow(
        title = edition.displayTitle,
        isSelected = currentEdition == edition.id,
        onClick = {
          MPVLib.setPropertyInt("edition", edition.id)
          onDismissRequest()
        },
        details = details,
      )
    },
    footer = {
      // DVD discs expose a synthetic "Disc Menu" edition; MPVLib.command
      // "disc-menu" enters the interactive disc menu. Blu-ray BD-J menus need
      // a Java runtime which Android lacks, so this is DVD-oriented.
      TextButton(
        onClick = {
          MPVLib.command("disc-menu")
          onDismissRequest()
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp),
      ) {
        Text(
          text = stringResource(R.string.player_sheets_disc_menu),
          fontWeight = FontWeight.Medium,
        )
      }
    },
  )
}
