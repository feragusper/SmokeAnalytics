import SwiftUI

/// Bottom tab bar mirroring the Android shell: Home, Analytics, History, Goals, You.
struct MainTabView: View {
    // Debug: launch with `-startTab home|analytics|history|goals|you` to open a specific tab
    // (parsed from launch args into UserDefaults by UIKit). No arg → Home.
    @State private var selection = MainTabView.initialTab()

    var body: some View {
        TabView(selection: $selection) {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }
                .tag(0)

            AnalyticsView()
                .tabItem { Label("Analytics", systemImage: "chart.bar.fill") }
                .tag(1)

            HistoryView()
                .tabItem { Label("History", systemImage: "clock.fill") }
                .tag(2)

            PlaceholderView(
                title: "Goals",
                systemImage: "target",
                message: "Set and track your goals soon."
            )
            .tabItem { Label("Goals", systemImage: "target") }
            .tag(3)

            SettingsView()
                .tabItem { Label("You", systemImage: "person.crop.circle") }
                .tag(4)
        }
        .tint(SA.primary)
    }

    private static func initialTab() -> Int {
        switch UserDefaults.standard.string(forKey: "startTab") {
        case "analytics": return 1
        case "history": return 2
        case "goals": return 3
        case "you": return 4
        default: return 0
        }
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
