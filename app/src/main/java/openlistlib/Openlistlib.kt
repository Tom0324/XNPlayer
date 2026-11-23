package openlistlib

abstract class Openlistlib {
  private constructor()

  external fun forceDBSync()
  external fun getOutboundIPString(): String
  external fun init(event: Event, logCallback: LogCallback)
  external fun isRunning(path: String): Boolean
  external fun setAdminPassword(password: String)
  external fun setConfigData(data: String)
  external fun setConfigDebug(value: Boolean)
  external fun setConfigLogStd(value: Boolean)
  external fun setConfigNoPrefix(value: Boolean)
  external fun shutdown(id: Long)
  external fun start()

  companion object {
    init {
      System.loadLibrary("gojni")
    }
  }
}
