import SwiftUI

/// Launch splash matching the Android system splash: brand-teal background (#249898) with the
/// white logo bars. Uses the same SplashBars image as the static launch screen so the cold-start /
/// widget launch reads as one continuous screen, then adds a gentle pulse before fading into the app.
struct SplashView: View {
    /// Brand teal, identical to Android `splash_background` (#249898) and the LaunchTeal asset.
    static let teal = Color(red: 0x24 / 255, green: 0x98 / 255, blue: 0x98 / 255)

    @State private var animate = false

    var body: some View {
        ZStack {
            Self.teal.ignoresSafeArea()
            Image("SplashBars")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 120, height: 120)
                .scaleEffect(animate ? 1.06 : 0.94)
                .opacity(animate ? 1 : 0.85)
                .animation(.easeInOut(duration: 0.6).repeatForever(autoreverses: true), value: animate)
        }
        .onAppear { DispatchQueue.main.async { animate = true } }
    }
}
