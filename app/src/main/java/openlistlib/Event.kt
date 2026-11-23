package openlistlib

interface Event {
  fun onProcessExit(code: Long)
  fun onShutdown(reason: String)
  fun onStartError(a: String, b: String)
}
