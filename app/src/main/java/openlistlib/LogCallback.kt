package openlistlib

interface LogCallback {
  fun onLog(level: Short, time: Long, msg: String)
}
