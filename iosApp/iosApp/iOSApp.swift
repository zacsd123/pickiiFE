import KakaoSDKCommon
import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitKoin()
        if let appKey = Bundle.main.object(forInfoDictionaryKey: "KakaoNativeAppKey") as? String,
            !appKey.isEmpty
        {
            KakaoSDK.initSDK(appKey: appKey)
        }
        KakaoAuthBridgeHolder.shared.bridge = KakaoAuthBridgeImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
