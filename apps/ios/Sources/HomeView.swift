import SwiftUI
import Shared

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var todayCount = 0
    @Published var weekCount = 0
    @Published var monthCount = 0
    @Published var lastSmokeEpochMillis: Int64 = -1
    @Published var nickname = ""
    @Published var quitReason = ""
    @Published var currencySymbol = "€"
    @Published var cigarettePrice = 0.0
    @Published var lastMonthCount = 0
    @Published var untaggedTodayCount = 0
    @Published var oldestUntaggedTodayId = ""
    @Published var points = 0
    @Published var streakHours = 0
    @Published var hasGoal = false
    @Published var goalTitle = ""
    @Published var goalStatus = ""
    @Published var goalFraction = -1.0
    @Published var isLoading = false
    @Published var errorText: String?
    @Published var triggerOptions: [TriggerChipItem] = []
    @Published var activeCravingSince: Date?

    private let facade = HomeFacade()
    private let cravingFacade = CravingFacade()
    private let goalsFacade = GoalsFacade()

    var spentToday: Double { Double(todayCount) * cigarettePrice }

    func load() async {
        isLoading = true
        errorText = nil
        do {
            apply(try await facade.load())
            let craving = try await cravingFacade.active()
            activeCravingSince = craving.map { Date(timeIntervalSince1970: Double($0.createdAtEpochMillis) / 1000.0) }
            if let g = try? await goalsFacade.load() {
                hasGoal = g.hasGoal
                goalTitle = g.title
                goalStatus = g.statusLabel
                goalFraction = g.progressFraction
            }
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
        nickname = s.nickname
        quitReason = s.quitReason
        currencySymbol = s.currencySymbol
        cigarettePrice = s.cigarettePrice
        lastMonthCount = Int(s.lastMonthCount)
        untaggedTodayCount = Int(s.untaggedTodayCount)
        oldestUntaggedTodayId = s.oldestUntaggedTodayId
        points = Int(s.points)
        streakHours = Int(s.streakHours)
    }

    var monthTrendDelta: Int { monthCount - lastMonthCount }

    func startNewDay() async {
        do { try await facade.startNewDay(); await load() }
        catch { errorText = String(describing: error) }
    }

    var greeting: String {
        let name = nickname.isEmpty ? nil : nickname
        let hour = Calendar.current.component(.hour, from: Date())
        let part: String
        switch hour {
        case 5..<12: part = "Good morning"
        case 12..<18: part = "Good afternoon"
        case 18..<22: part = "Good evening"
        default: part = "Good night"
        }
        return name.map { "\(part), \($0)" } ?? part
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
                        if viewModel.untaggedTodayCount > 0 { relationshipReminderCard }
                        if viewModel.hasGoal { goalCard }
                        HStack(spacing: 16) {
                            statCard("This week", viewModel.weekCount)
                            statCard("This month", viewModel.monthCount)
                        }
                        if viewModel.lastMonthCount > 0 || viewModel.monthCount > 0 { trendCard }
                        if viewModel.points > 0 { pointsCard }
                        if viewModel.cigarettePrice > 0 { spentTodayCard }
                        smokeButton
                        cravingSection
                        if !viewModel.quitReason.isEmpty { quitReasonCard }
                        startNewDayButton

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
            .navigationTitle(viewModel.greeting)
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

    private var goalCard: some View {
        SACard {
            HStack(spacing: 14) {
                ZStack {
                    Circle().stroke(SA.surfaceVariant, lineWidth: 6)
                    if viewModel.goalFraction >= 0 {
                        Circle()
                            .trim(from: 0, to: min(max(viewModel.goalFraction, 0), 1))
                            .stroke(SA.primary, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                            .rotationEffect(.degrees(-90))
                    }
                    Image(systemName: "target").font(.system(size: 16)).foregroundStyle(SA.primary)
                }
                .frame(width: 44, height: 44)
                VStack(alignment: .leading, spacing: 2) {
                    Text(viewModel.goalTitle).font(.saTitleMedium).foregroundStyle(SA.onSurface)
                    Text(viewModel.goalStatus).font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
                }
                Spacer()
            }
        }
    }

    private var relationshipReminderCard: some View {
        Button {
            if !viewModel.oldestUntaggedTodayId.isEmpty {
                pendingSmoke = PendingSmoke(id: viewModel.oldestUntaggedTodayId)
            }
        } label: {
            SACard {
                HStack(spacing: 14) {
                    Image(systemName: "tag.fill").font(.system(size: 20)).foregroundStyle(SA.primary)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("\(viewModel.untaggedTodayCount) \(viewModel.untaggedTodayCount == 1 ? "cigarette needs" : "cigarettes need") a tag")
                            .font(.saTitleMedium).foregroundStyle(SA.onSurface)
                        Text("Tap to add what triggered it").font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
                    }
                    Spacer()
                    Image(systemName: "chevron.right").foregroundStyle(SA.onSurfaceVariant)
                }
            }
        }
        .buttonStyle(.plain)
    }

    private var trendCard: some View {
        let delta = viewModel.monthTrendDelta
        let improving = delta < 0
        return SACard {
            HStack(spacing: 14) {
                Image(systemName: improving ? "arrow.down.right" : (delta > 0 ? "arrow.up.right" : "equal"))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(delta == 0 ? SA.onSurfaceVariant : (improving ? SA.primary : SA.error))
                VStack(alignment: .leading, spacing: 2) {
                    Text("This month vs last").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                    Text(delta == 0 ? "Same as last month"
                         : "\(abs(delta)) \(abs(delta) == 1 ? "cigarette" : "cigarettes") \(improving ? "fewer" : "more")")
                        .font(.saTitleMedium).foregroundStyle(SA.onSurface)
                }
                Spacer()
            }
        }
    }

    private var pointsCard: some View {
        SACard {
            HStack(spacing: 14) {
                Image(systemName: "trophy.fill").font(.system(size: 22)).foregroundStyle(SA.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Points").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                    Text("\(viewModel.points)").font(.saHeadlineSmall).foregroundStyle(SA.onSurface)
                }
                Spacer()
                if viewModel.streakHours > 0 {
                    VStack(alignment: .trailing, spacing: 2) {
                        Text("Streak").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                        Text("\(viewModel.streakHours)h").font(.saTitleMedium).foregroundStyle(SA.primary)
                    }
                }
            }
        }
    }

    private var startNewDayButton: some View {
        Button {
            Task { await viewModel.startNewDay() }
        } label: {
            Text("Start a new day")
                .font(.saBodyLarge)
                .foregroundStyle(SA.onSurfaceVariant)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
        }
        .disabled(viewModel.isLoading)
        .padding(.top, 4)
    }

    private var spentTodayCard: some View {
        SACard {
            HStack(spacing: 14) {
                Image(systemName: "creditcard.fill").font(.system(size: 22)).foregroundStyle(SA.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Spent today").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                    Text("\(viewModel.currencySymbol)\(viewModel.spentToday, specifier: "%.2f")")
                        .font(.saHeadlineSmall).foregroundStyle(SA.onSurface)
                }
                Spacer()
            }
        }
    }

    private var quitReasonCard: some View {
        SACard {
            HStack(spacing: 14) {
                Image(systemName: "quote.opening").font(.system(size: 20)).foregroundStyle(SA.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Your reason to quit").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                    Text(viewModel.quitReason).font(.saBodyLarge).foregroundStyle(SA.onSurface)
                }
                Spacer()
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
