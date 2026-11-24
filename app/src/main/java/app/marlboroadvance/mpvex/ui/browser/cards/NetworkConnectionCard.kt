package app.marlboroadvance.mpvex.ui.browser.cards

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.marlboroadvance.mpvex.domain.network.NetworkConnection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkConnectionCard(
  connection: NetworkConnection,
  onConnect: (NetworkConnection) -> Unit,
  onDisconnect: (NetworkConnection) -> Unit,
  onEdit: (NetworkConnection) -> Unit,
  onDelete: (NetworkConnection) -> Unit,
  onBrowse: (NetworkConnection) -> Unit,
  onAutoConnectChange: (NetworkConnection, Boolean) -> Unit,
  modifier: Modifier = Modifier,
  isConnected: Boolean = false,
  isConnecting: Boolean = false,
  error: String? = null,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  val statusText = when {
    isConnecting -> "连接中"
    isConnected -> "已连接"
    error != null -> "连接异常"
    else -> "未连接"
  }
  val statusColor = when {
    isConnecting -> MaterialTheme.colorScheme.onSurfaceVariant
    isConnected -> MaterialTheme.colorScheme.primary
    error != null -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = CardDefaults.outlinedCardBorder(),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = connection.name.ifBlank { "无" },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { if (isConnected) onBrowse(connection) else onConnect(connection) }) {
          Icon(Icons.Filled.Link, contentDescription = "连接", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
          IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text(if (isConnected) "浏览" else "连接") },
              onClick = {
                menuExpanded = false
                if (isConnected) onBrowse(connection) else onConnect(connection)
              },
              leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            )
            DropdownMenuItem(
              text = { Text("编辑") },
              onClick = { menuExpanded = false; onEdit(connection) },
              leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            )
            DropdownMenuItem(
              text = { Text("应用启动时自动连接") },
              onClick = {
                menuExpanded = false
                onAutoConnectChange(connection, !connection.autoConnect)
              },
              leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            )
            if (isConnected) {
              DropdownMenuItem(
                text = { Text("断开连接") },
                onClick = { menuExpanded = false; onDisconnect(connection) },
                leadingIcon = { Icon(Icons.Filled.LinkOff, contentDescription = null) },
              )
            }
            DropdownMenuItem(
              text = { Text("删除") },
              onClick = { menuExpanded = false; onDelete(connection) },
              leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
          shape = RoundedCornerShape(6.dp),
        ) {
          Text(
            text = connection.protocol.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("● $statusText", style = MaterialTheme.typography.labelMedium, color = statusColor)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "${connection.host}:${connection.port}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (connection.path != "/") {
        Text(
          text = "路径：${connection.path}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      if (error != null) {
        Text(
          text = "错误：$error",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

