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
    @Published var activeCravingSince: Date?

    private let facade = HomeFacade()
    private let cravingFacade = CravingFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            apply(try await facade.load())
            let craving = try await cravingFacade.active()
            activeCravingSince = craving.map { Date(timeIntervalSince1970: Double($0.createdAtEpochMillis) / 1000.0) }
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
        if triggerOptions.isEmpty {
            triggerOptions = (try? await facade.triggerOptions())?
                .map { TriggerChipItem(id: $0.key, display: $0.display) } ?? []
        }
    }

    func startCraving() async {
        do { try await cravingFacade.start(); await load() }
        catch { errorText = String(describing: error) }
    }

    func resolveCravingResisted() async {
        do { try await cravingFacade.resolveResisted(); await load() }
        catch { errorText = String(describing: error) }
    }

    func resolveCravingGaveIn() async {
        do { try await cravingFacade.resolveGaveIn(); await load() }
        catch { errorText = String(describing: error) }
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
                        cravingSection

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

    @ViewBuilder
    private var cravingSection: some View {
        if let since = viewModel.activeCravingSince {
            cravingActiveCard(since)
        } else {
            Button {
                Task { await viewModel.startCraving() }
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "hand.raised.fill")
                    Text("Track a craving")
                }
                .font(.saTitleMedium)
                .foregroundStyle(SA.primary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .overlay(
                    RoundedRectangle(cornerRadius: 20, style: .continuous)
                        .strokeBorder(SA.primary, lineWidth: 1.5)
                )
            }
            .disabled(viewModel.isLoading)
        }
    }

    private func cravingActiveCard(_ since: Date) -> some View {
        SACard(cornerRadius: 24) {
            VStack(alignment: .leading, spacing: 14) {
                HStack(spacing: 10) {
                    Image(systemName: "hand.raised.fill").foregroundStyle(SA.primary)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Resisting a craving").font(.saTitleMedium).foregroundStyle(SA.onSurface)
                        Text("Started \(RelativeDateTimeFormatter().localizedString(for: since, relativeTo: Date()))")
                            .font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
                    }
                    Spacer()
                }
                HStack(spacing: 12) {
                    Button {
                        Task { await viewModel.resolveCravingResisted() }
                    } label: {
                        Text("I'm good")
                            .font(.saTitleMedium).foregroundStyle(SA.onPrimary)
                            .frame(maxWidth: .infinity).padding(.vertical, 12)
                            .background(SA.primary, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                    }
                    Button {
                        Task { await viewModel.resolveCravingGaveIn() }
                    } label: {
                        Text("I smoked")
                            .font(.saTitleMedium).foregroundStyle(SA.primary)
                            .frame(maxWidth: .infinity).padding(.vertical, 12)
                            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).strokeBorder(SA.primary, lineWidth: 1.5))
                    }
                }
            }
        }
    }
}
