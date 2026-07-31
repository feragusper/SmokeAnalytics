import SwiftUI

/// Bottom tab bar mirroring the Android shell: Home, Analytics, History, Goals, You.
struct MainTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }

            PlaceholderView(
                title: "Analytics",
                systemImage: "chart.bar.fill",
                message: "Charts and trends land here next."
            )
            .tabItem { Label("Analytics", systemImage: "chart.bar.fill") }

            HistoryView()
                .tabItem { Label("History", systemImage: "clock.fill") }

            PlaceholderView(
                title: "Goals",
                systemImage: "target",
                message: "Set and track your goals soon."
            )
            .tabItem { Label("Goals", systemImage: "target") }

            SettingsView()
                .tabItem { Label("You", systemImage: "person.crop.circle") }
        }
        .tint(SA.primary)
    }
}

/// Themed empty state for tabs not built yet.
struct PlaceholderView: View {
    let title: String
    let systemImage: String
    let message: String

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                VStack(spacing: 12) {
                    Image(systemName: systemImage)
                        .font(.system(size: 44))
                        .foregroundStyle(SA.primary)
                    Text(message)
                        .font(.saBodyLarge)
                        .foregroundStyle(SA.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                }
            }
            .navigationTitle(title)
        }
    }
}
