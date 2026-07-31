import SwiftUI
import Shared

@MainActor
final class GoalsViewModel: ObservableObject {
    @Published var hasGoal = false
    @Published var title = ""
    @Published var detail = ""
    @Published var statusLabel = ""
    @Published var progressFraction = -1.0
    @Published var streakDays = 0
    @Published var goalTypeKey = ""
    @Published var goalValue = 0
    @Published var isLoading = false
    @Published var errorText: String?

    private let facade = GoalsFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            let s = try await facade.load()
            hasGoal = s.hasGoal
            title = s.title
            detail = s.detail
            statusLabel = s.statusLabel
            progressFraction = s.progressFraction
            streakDays = Int(s.streakDays)
            goalTypeKey = s.goalTypeKey
            goalValue = Int(s.goalValue)
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }

    func saveGoal(_ typeKey: String, _ value: Int) async {
        do {
            try await facade.saveGoal(typeKey: typeKey, value: Int32(value))
            await load()
        } catch {
            errorText = String(describing: error)
        }
    }

    func clearGoal() async {
        do {
            try await facade.clearGoal()
            await load()
        } catch {
            errorText = String(describing: error)
        }
    }
}

struct GoalsView: View {
    @StateObject private var viewModel = GoalsViewModel()
    @State private var showEditor = false

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 16) {
                        if viewModel.hasGoal {
                            goalCard
                            if viewModel.streakDays > 0 { streakCard }
                        } else if !viewModel.isLoading {
                            emptyState
                        }
                        if let error = viewModel.errorText {
                            Text(error).font(.saBodyMedium).foregroundStyle(SA.error)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("Goals")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(viewModel.hasGoal ? "Edit" : "Set") { showEditor = true }
                        .tint(SA.primary)
                }
            }
            .task {
                await viewModel.load()
                // Debug: `-showGoalEditor` opens the editor on launch (no write until Save).
                if UserDefaults.standard.bool(forKey: "showGoalEditor") { showEditor = true }
            }
            .refreshable { await viewModel.load() }
            .overlay { if viewModel.isLoading { ProgressView().tint(SA.primary) } }
            .sheet(isPresented: $showEditor) {
                GoalEditorSheet(
                    prefillTypeKey: viewModel.goalTypeKey,
                    prefillValue: viewModel.goalValue,
                    isEditing: viewModel.hasGoal,
                    onSave: { key, value in Task { await viewModel.saveGoal(key, value) } },
                    onClear: { Task { await viewModel.clearGoal() } }
                )
                .presentationDetents([.large])
            }
        }
    }

    private var goalCard: some View {
        SACard(cornerRadius: 28) {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(viewModel.title)
                            .font(.saTitleMedium)
                            .foregroundStyle(SA.onSurface)
                        Text(viewModel.detail)
                            .font(.saBodyMedium)
                            .foregroundStyle(SA.onSurfaceVariant)
                    }
                    Spacer()
                    statusChip
                }
                if viewModel.progressFraction >= 0 {
                    progressRing
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }

    private var statusChip: some View {
        Text(viewModel.statusLabel)
            .font(.saLabelMedium)
            .foregroundStyle(SA.onPrimary)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(SA.primary, in: Capsule())
    }

    private var progressRing: some View {
        ZStack {
            Circle()
                .stroke(SA.surfaceVariant, lineWidth: 14)
            Circle()
                .trim(from: 0, to: min(max(viewModel.progressFraction, 0), 1))
                .stroke(SA.primary, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                .rotationEffect(.degrees(-90))
            Text("\(Int((min(max(viewModel.progressFraction, 0), 1)) * 100))%")
                .font(.system(size: 40, weight: .bold))
                .foregroundStyle(SA.onSurface)
        }
        .frame(width: 160, height: 160)
        .padding(.vertical, 8)
    }

    private var streakCard: some View {
        SACard {
            HStack(spacing: 12) {
                Image(systemName: "flame.fill")
                    .font(.system(size: 28))
                    .foregroundStyle(SA.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(viewModel.streakDays)-day streak")
                        .font(.saTitleMedium)
                        .foregroundStyle(SA.onSurface)
                    Text("Days you stayed within your goal")
                        .font(.saBodyMedium)
                        .foregroundStyle(SA.onSurfaceVariant)
                }
                Spacer()
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "target")
                .font(.system(size: 44))
                .foregroundStyle(SA.primary)
            Text("No active goal")
                .font(.saTitleMedium)
                .foregroundStyle(SA.onSurface)
            Text("Tap \u{201C}Set\u{201D} to choose a goal and track your progress here.")
                .font(.saBodyLarge)
                .foregroundStyle(SA.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button("Set a goal") { showEditor = true }
                .buttonStyle(SAPrimaryButtonStyle())
                .padding(.horizontal, 40)
                .padding(.top, 8)
        }
        .padding(.top, 60)
    }
}
