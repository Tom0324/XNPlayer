package openlistlib

abstract class Openlistlib private constructor() {
  companion object {
    init {
      System.loadLibrary("gojni")
    }

    @JvmStatic external fun forceDBSync()
    @JvmStatic external fun getOutboundIPString(): String
    @JvmStatic external fun init(event: Event, logCallback: LogCallback)
    @JvmStatic external fun isRunning(path: String): Boolean
    @JvmStatic external fun setAdminPassword(password: String)
    @JvmStatic external fun setConfigData(data: String)
    @JvmStatic external fun setConfigDebug(value: Boolean)
    @JvmStatic external fun setConfigLogStd(value: Boolean)
    @JvmStatic external fun setConfigNoPrefix(value: Boolean)
    @JvmStatic external fun shutdown(id: Long)
    @JvmStatic external fun start()
  }
}
