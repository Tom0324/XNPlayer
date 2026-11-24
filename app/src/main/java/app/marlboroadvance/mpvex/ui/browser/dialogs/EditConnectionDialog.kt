package app.marlboroadvance.mpvex.ui.browser.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.marlboroadvance.mpvex.domain.network.NetworkConnection
import app.marlboroadvance.mpvex.domain.network.NetworkProtocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConnectionSheet(
  connection: NetworkConnection,
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onSave: (NetworkConnection) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (!isOpen) return

  var name by remember(connection.id) { mutableStateOf(connection.name) }
  var protocol by remember(connection.id) { mutableStateOf(connection.protocol) }
  var host by remember(connection.id) { mutableStateOf(connection.host) }
  var port by remember(connection.id) { mutableStateOf(connection.port.toString()) }
  var username by remember(connection.id) { mutableStateOf(connection.username) }
  var password by remember(connection.id) { mutableStateOf(connection.password) }
  var path by remember(connection.id) { mutableStateOf(connection.path) }
  var isAnonymous by remember(connection.id) { mutableStateOf(connection.isAnonymous) }
  var useHttps by remember(connection.id) { mutableStateOf(connection.useHttps) }
  var passwordVisible by remember { mutableStateOf(false) }

  val handleSave = {
    val updatedConnection =
      connection.copy(
        name = name,
        protocol = protocol,
        host = host,
        port = port.toIntOrNull() ?: protocol.defaultPort,
        username = if (isAnonymous) "" else username,
        password = if (isAnonymous) "" else password,
        path = path.ifBlank { "/" },
        isAnonymous = isAnonymous,
        useHttps = useHttps,
      )
    onSave(updatedConnection)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "编辑连接",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = name.ifBlank { "无" },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(12.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text("协议", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(10.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NetworkProtocol.entries.forEach { proto ->
                  val selected = proto == protocol
                  val onClick = {
                    protocol = proto
                    port = proto.defaultPort.toString()
                  }
                  if (selected) {
                    FilledTonalButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                      Text(proto.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                  } else {
                    OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                      Text(proto.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                  }
                }
              }
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(12.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("基础信息", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
              OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("主机/IP 地址") }, leadingIcon = { Icon(Icons.Filled.Language, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("192.168.1.100") })
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("端口") }, leadingIcon = { Icon(Icons.Filled.Numbers, null) }, modifier = Modifier.weight(0.35f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("路径") }, leadingIcon = { Icon(Icons.Filled.Folder, null) }, modifier = Modifier.weight(0.65f), singleLine = true, placeholder = { Text("/") })
              }
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(12.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PersonOff, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("匿名/访客访问", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
              }
              OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("用户名") }, leadingIcon = { Icon(Icons.Filled.Person, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isAnonymous)
              OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("密码") }, leadingIcon = { Icon(Icons.Filled.Lock, null) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isAnonymous)
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(12.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("使用 HTTPS（安全连接）", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = useHttps, onCheckedChange = { useHttps = it; if (it && port == "80") port = "443" else if (!it && port == "443") port = "80" }, enabled = protocol == NetworkProtocol.WEBDAV)
              }
              OutlinedTextField(value = "", onValueChange = {}, label = { Text("User-Agent（可选）") }, leadingIcon = { Icon(Icons.Filled.Language, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = false)
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = onDismiss) { Text("取消") }
          Spacer(modifier = Modifier.width(8.dp))
          FilledTonalButton(onClick = handleSave, enabled = host.isNotBlank() && (isAnonymous || username.isNotBlank())) { Text("保存", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}
