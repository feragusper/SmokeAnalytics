import SwiftUI
import Charts
import Shared

struct DayBar: Identifiable {
    let id = UUID()
    let label: String
    let count: Int
}

struct TriggerRow: Identifiable {
    let id = UUID()
    let label: String
    let count: Int
}

@MainActor
final class AnalyticsViewModel: ObservableObject {
    @Published var period = "month"
    @Published var periodTotal = 0
    @Published var bars: [DayBar] = []
    @Published var triggers: [TriggerRow] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let facade = StatsFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            let s = try await facade.load(periodKey: period)
            periodTotal = Int(s.periodTotal)
            bars = s.bars.map { DayBar(label: $0.label, count: Int($0.count)) }
            triggers = s.triggers.map { TriggerRow(label: $0.label, count: Int($0.count)) }
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }
}

struct AnalyticsView: View {
    @StateObject private var viewModel = AnalyticsViewModel()

    private let periods: [(key: String, label: String)] = [
        ("day", "Day"), ("week", "Week"), ("month", "Month"), ("year", "Year"),
    ]

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 16) {
                        Picker("Period", selection: $viewModel.period) {
                            ForEach(periods, id: \.key) { Text($0.label).tag($0.key) }
                        }
                        .pickerStyle(.segmented)
                        .onChange(of: viewModel.period) { _ in Task { await viewModel.load() } }

                        HStack(spacing: 16) {
                            summary("Total", "\(viewModel.periodTotal)")
                            summary("Daily avg", dailyAverage.formatted(.number.precision(.fractionLength(1))))
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

    /// True per-day average across the selected period (total / days in that period).
    private var dailyAverage: Double {
        let cal = Calendar.current
        let now = Date()
        let days: Int
        switch viewModel.period {
        case "day": days = 1
        case "week": days = 7
        case "year": days = cal.range(of: .day, in: .year, for: now)?.count ?? 365
        default: days = cal.range(of: .day, in: .month, for: now)?.count ?? 30
        }
        return days > 0 ? Double(viewModel.periodTotal) / Double(days) : 0
    }

    private var chartTitle: String {
        switch viewModel.period {
        case "day": return "Cigarettes per hour"
        case "week": return "Cigarettes per weekday"
        case "year": return "Cigarettes per month"
        default: return "Cigarettes per week"
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
                Text(chartTitle)
                    .font(.saTitleMedium)
                    .foregroundStyle(SA.onSurface)
                Chart(viewModel.bars) { bar in
                    AreaMark(
                        x: .value("Bucket", bar.label),
                        y: .value("Count", bar.count)
                    )
                    .interpolationMethod(.catmullRom)
                    .foregroundStyle(LinearGradient(
                        colors: [SA.primary.opacity(0.25), SA.primary.opacity(0.02)],
                        startPoint: .top, endPoint: .bottom
                    ))
                    LineMark(
                        x: .value("Bucket", bar.label),
                        y: .value("Count", bar.count)
                    )
                    .interpolationMethod(.catmullRom)
                    .foregroundStyle(SA.primary)
                    .lineStyle(StrokeStyle(lineWidth: 2.5))
                }
                .frame(height: 200)
                .chartXAxis {
                    AxisMarks(values: .automatic(desiredCount: 6)) { AxisValueLabel() }
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
                        Text(trigger.label).font(.saBodyLarge).foregroundStyle(SA.onSurface)
                        Spacer()
                        Text("\(trigger.count)").font(.saBodyLarge).foregroundStyle(SA.primary)
                    }
                }
            }
        }
    }
}
