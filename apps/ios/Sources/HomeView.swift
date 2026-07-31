import SwiftUI
import Shared

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var todayCount = 0
    @Published var weekCount = 0
    @Published var monthCount = 0
    @Published var lastSmokeEpochMillis: Int64 = -1
    @Published var isLoading = false
    @Published var errorText: String?
    @Published var triggerOptions: [TriggerChipItem] = []

    private let facade = HomeFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            apply(try await facade.load())
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
        if triggerOptions.isEmpty {
            triggerOptions = (try? await facade.triggerOptions())?
                .map { TriggerChipItem(id: $0.key, display: $0.display) } ?? []
        }
    }

    /// Logs a smoke and returns its id so Home can prompt for the trigger; nil on failure.
    func logSmoke() async -> String? {
        isLoading = true
        errorText = nil
        defer { isLoading = false }
        do {
            return try await facade.logSmoke()
        } catch {
            errorText = String(describing: error)
            return nil
        }
    }

    func setRelationship(id: String, tags: [String]) async {
        if id == "__preview__" { return } // debug -previewTriggers: no write
        do {
            try await facade.setRelationship(id: id, tags: tags)
            await load()
        } catch {
            errorText = String(describing: error)
        }
    }

    private func apply(_ s: HomeSnapshot) {
        todayCount = Int(s.todayCount)
        weekCount = Int(s.weekCount)
        monthCount = Int(s.monthCount)
        lastSmokeEpochMillis = s.lastSmokeEpochMillis
    }

    var lastSmokeText: String {
        guard lastSmokeEpochMillis >= 0 else { return "No smokes logged yet" }
        let date = Date(timeIntervalSince1970: Double(lastSmokeEpochMillis) / 1000.0)
        let fmt = RelativeDateTimeFormatter()
        fmt.unitsStyle = .full
        return "Last cigarette " + fmt.localizedString(for: date, relativeTo: Date())
    }
}

private struct PendingSmoke: Identifiable {
    let id: String
}

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @EnvironmentObject private var auth: AuthManager
    @State private var pendingSmoke: PendingSmoke?

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 16) {
                        heroCard
                        HStack(spacing: 16) {
                            statCard("This week", viewModel.weekCount)
                            statCard("This month", viewModel.monthCount)
                        }
                        smokeButton

                        if let error = viewModel.errorText {
                            Text(error)
                                .font(.saBodyMedium)
                                .foregroundStyle(SA.error)
                                .multilineTextAlignment(.center)
                                .padding(.top, 4)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle(auth.displayName.map { "Hi, \($0)" } ?? "Home")
            .navigationBarTitleDisplayMode(.inline)
            .task {
                await viewModel.load()
                // Debug: `-previewTriggers` opens the trigger sheet on launch (no smoke written).
                if UserDefaults.standard.bool(forKey: "previewTriggers") {
                    pendingSmoke = PendingSmoke(id: "__preview__")
                }
            }
            .sheet(item: $pendingSmoke) { pending in
                RelationshipSheet(
                    options: viewModel.triggerOptions,
                    onSave: { tags in Task { await viewModel.setRelationship(id: pending.id, tags: tags) } },
                    onSkip: { Task { await viewModel.setRelationship(id: pending.id, tags: []) } }
                )
                .presentationDetents([.medium, .large])
            }
        }
        .tint(SA.primary)
    }

    private var heroCard: some View {
        SACard(cornerRadius: 28) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Today")
                    .font(.saLabelLarge)
                    .foregroundStyle(SA.onSurfaceVariant)
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text("\(viewModel.todayCount)")
                        .font(.system(size: 72, weight: .bold))
                        .foregroundStyle(SA.primary)
                        .contentTransition(.numericText())
                    Text(viewModel.todayCount == 1 ? "cigarette" : "cigarettes")
                        .font(.saTitleMedium)
                        .foregroundStyle(SA.onSurfaceVariant)
                }
                Text(viewModel.lastSmokeText)
                    .font(.saBodyMedium)
                    .foregroundStyle(SA.onSurfaceVariant)
            }
        }
    }

    private func statCard(_ title: String, _ value: Int) -> some View {
        SACard {
            VStack(alignment: .leading, spacing: 6) {
                Text(title)
                    .font(.saLabelMedium)
                    .foregroundStyle(SA.onSurfaceVariant)
                Text("\(value)")
                    .font(.saHeadlineSmall)
                    .foregroundStyle(SA.onSurface)
            }
        }
    }

    private var smokeButton: some View {
        Button {
            Task {
                if let id = await viewModel.logSmoke() {
                    pendingSmoke = PendingSmoke(id: id)
                }
            }
        } label: {
            HStack(spacing: 10) {
                Image(systemName: "plus.circle.fill")
                Text("I smoked")
            }
        }
        .buttonStyle(SAPrimaryButtonStyle())
        .disabled(viewModel.isLoading)
        .padding(.top, 8)
    }
}
