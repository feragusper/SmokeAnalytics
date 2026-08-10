import SwiftUI

/// Auth gate: the Home screen once signed in, otherwise the sign-in screen.
struct RootView: View {
    @EnvironmentObject private var auth: AuthManager
    // Device-local theme override, like the Android appearance setting.
    @AppStorage("themeMode") private var themeMode = "system"
    // Observed so changing the accent re-renders and re-tints via SA.primary.
    @AppStorage("accent") private var accent = "default"

    var body: some View {
        Group {
            if auth.isSignedIn {
                MainTabView()
            } else {
                SignInView()
            }
        }
        .tint(SA.primary)
        .preferredColorScheme(colorScheme)
    }

    private var colorScheme: ColorScheme? {
        switch themeMode {
        case "light": return .light
        case "dark": return .dark
        default: return nil
        }
    }
}
