import SwiftUI
import FirebaseCore
import GoogleSignIn
import Shared

/// One-time Firebase + Koin startup. Runs from `AuthManager.init()` so it always happens before any
/// `Auth.auth()` / GitLive access, regardless of SwiftUI property-init ordering.
enum AppBootstrap {
    private static var done = false
    static func ensure() {
        guard !done else { return }
        done = true
        FirebaseApp.configure()
        KoinKt.doInitKoin()
    }
}

@main
struct SmokeAnalyticsApp: App {
    @StateObject private var auth = AuthManager()
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            ZStack {
                RootView()
                    .environmentObject(auth)
                    .onOpenURL { url in
                        GIDSignIn.sharedInstance.handle(url)
                    }
                if showSplash {
                    SplashView()
                        .transition(.opacity)
                        .task {
                            try? await Task.sleep(nanoseconds: 1_200_000_000)
                            withAnimation(.easeOut(duration: 0.35)) { showSplash = false }
                        }
                }
            }
        }
    }
}
