import SwiftUI

/// Branded launch splash: the app mark on the brand background, animating out into the app —
/// the iOS counterpart of the Android animated splash.
struct SplashView: View {
    @State private var appeared = false

    var body: some View {
        ZStack {
            SA.background.ignoresSafeArea()
            Image("AppLogo")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 120, height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 27, style: .continuous))
                .scaleEffect(appeared ? 1 : 0.85)
                .opacity(appeared ? 1 : 0)
                .shadow(color: .black.opacity(0.12), radius: 16, y: 8)
        }
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.7)) { appeared = true }
        }
    }
}
