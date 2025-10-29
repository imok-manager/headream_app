import SwiftUI
import AVFoundation

@main
struct iOSApp: App {
    init() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [])
            try session.setActive(true)
            #if DEBUG
            print("[Audio] AVAudioSession configured: playback mode")
            #endif
        } catch {
            print("[Audio] Failed to configure AVAudioSession: \(error)")
        }
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
