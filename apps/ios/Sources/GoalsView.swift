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
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }
}

struct GoalsView: View {
    @StateObject private var viewModel = GoalsViewModel()

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
            .task { await viewModel.load() }
            .refreshable { await viewModel.load() }
            .overlay { if viewModel.isLoading { ProgressView().tint(SA.primary) } }
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
            Text("Set a goal on Android or the web app and your progress will show up here.")
                .font(.saBodyLarge)
                .foregroundStyle(SA.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .padding(.top, 60)
    }
}
