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

    private let facade = HomeFacade()

    func load() async {
        await run { try await self.facade.load() }
    }

    func addSmoke() async {
        await run { try await self.facade.addSmokeNow() }
    }

    private func run(_ work: @escaping () async throws -> HomeSnapshot) async {
        isLoading = true
        errorText = nil
        do {
            apply(try await work())
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
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

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @EnvironmentObject private var auth: AuthManager

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
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { auth.signOut() } label: {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                    }
                    .tint(SA.primary)
                }
            }
            .task { await viewModel.load() }
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
            Task { await viewModel.addSmoke() }
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
