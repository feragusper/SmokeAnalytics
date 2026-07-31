import SwiftUI
import Charts
import Shared

struct DayBar: Identifiable {
    let id = UUID()
    let day: String
    let count: Int
}

struct TriggerRow: Identifiable {
    let id = UUID()
    let label: String
    let count: Int
}

@MainActor
final class AnalyticsViewModel: ObservableObject {
    @Published var totalMonth = 0
    @Published var totalWeek = 0
    @Published var dailyAverage = 0.0
    @Published var bars: [DayBar] = []
    @Published var triggers: [TriggerRow] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let facade = StatsFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            let s = try await facade.loadCurrentMonth()
            totalMonth = Int(s.totalMonth)
            totalWeek = Int(s.totalWeek)
            dailyAverage = s.dailyAverage
            bars = s.dailyBars.map { DayBar(day: $0.label, count: Int($0.count)) }
            triggers = s.triggers.map { TriggerRow(label: $0.label, count: Int($0.count)) }
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }
}

struct AnalyticsView: View {
    @StateObject private var viewModel = AnalyticsViewModel()

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 16) {
                        HStack(spacing: 16) {
                            summary("This month", "\(viewModel.totalMonth)")
                            summary("This week", "\(viewModel.totalWeek)")
                            summary("Daily avg", String(format: "%.1f", viewModel.dailyAverage))
                        }
                        chartCard
                        if !viewModel.triggers.isEmpty { triggerCard }
                        if let error = viewModel.errorText {
                            Text(error).font(.saBodyMedium).foregroundStyle(SA.error)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("Analytics")
            .task { await viewModel.load() }
            .refreshable { await viewModel.load() }
            .overlay { if viewModel.isLoading { ProgressView().tint(SA.primary) } }
        }
    }

    private func summary(_ title: String, _ value: String) -> some View {
        SACard {
            VStack(alignment: .leading, spacing: 6) {
                Text(title).font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                Text(value).font(.saHeadlineSmall).foregroundStyle(SA.onSurface)
            }
        }
    }

    private var chartCard: some View {
        SACard(cornerRadius: 28) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Cigarettes per day")
                    .font(.saTitleMedium)
                    .foregroundStyle(SA.onSurface)
                Chart(viewModel.bars) { bar in
                    BarMark(
                        x: .value("Day", bar.day),
                        y: .value("Count", bar.count)
                    )
                    .foregroundStyle(SA.primary)
                    .cornerRadius(3)
                }
                .frame(height: 200)
                .chartXAxis {
                    AxisMarks(values: .automatic(desiredCount: 6)) {
                        AxisValueLabel()
                    }
                }
            }
        }
    }

    private var triggerCard: some View {
        SACard {
            VStack(alignment: .leading, spacing: 12) {
                Text("Top triggers")
                    .font(.saTitleMedium)
                    .foregroundStyle(SA.onSurface)
                ForEach(viewModel.triggers) { trigger in
                    HStack {
                        Text(trigger.label)
                            .font(.saBodyLarge)
                            .foregroundStyle(SA.onSurface)
                        Spacer()
                        Text("\(trigger.count)")
                            .font(.saBodyLarge)
                            .foregroundStyle(SA.primary)
                    }
                }
            }
        }
    }
}
