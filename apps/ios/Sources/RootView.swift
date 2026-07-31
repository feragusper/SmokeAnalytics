import SwiftUI

/// Auth gate: the Home screen once signed in, otherwise the sign-in screen.
struct RootView: View {
    @EnvironmentObject private var auth: AuthManager

    var body: some View {
        Group {
            if auth.isSignedIn {
                HomeView()
            } else {
                SignInView()
            }
        }
        .tint(SA.primary)
    }
}
