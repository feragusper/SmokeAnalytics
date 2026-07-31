import SwiftUI
import Shared

struct HistoryEntry: Identifiable {
    let id: String
    let date: Date
    let minutesSincePrevious: Int64
}

struct HistorySection: Identifiable {
    let id: Date
    let title: String
    let entries: [HistoryEntry]
}

@MainActor
final class HistoryViewModel: ObservableObject {
    @Published var sections: [HistorySection] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let facade = HistoryFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            let items = try await facade.loadMonth()
            sections = Self.group(items)
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }

    func delete(_ id: String) async {
        do {
            try await facade.delete(id: id)
            await load()
        } catch {
            errorText = String(describing: error)
        }
    }

    private static func group(_ items: [SmokeItem]) -> [HistorySection] {
        let cal = Calendar.current
        let entries = items.map {
            HistoryEntry(
                id: $0.id,
                date: Date(timeIntervalSince1970: Double($0.epochMillis) / 1000.0),
                minutesSincePrevious: $0.minutesSincePrevious
            )
        }
        let grouped = Dictionary(grouping: entries) { cal.startOfDay(for: $0.date) }
        let fmt = DateFormatter()
        fmt.dateFormat = "EEEE, d MMM"
        return grouped.keys.sorted(by: >).map { day in
            HistorySection(
                id: day,
                title: fmt.string(from: day),
                entries: grouped[day]!.sorted { $0.date > $1.date }
            )
        }
    }
}

struct HistoryView: View {
    @StateObject private var viewModel = HistoryViewModel()

    private static let timeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.timeStyle = .short
        return f
    }()

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()

                if viewModel.sections.isEmpty && !viewModel.isLoading {
                    emptyState
                } else {
                    List {
                        ForEach(viewModel.sections) { section in
                            Section(section.title) {
                                ForEach(section.entries) { entry in
                                    row(entry)
                                        .listRowBackground(SA.surfaceContainer)
                                        .swipeActions {
                                            Button(role: .destructive) {
                                                Task { await viewModel.delete(entry.id) }
                                            } label: {
                                                Label("Delete", systemImage: "trash")
                                            }
                                        }
                                }
                            }
                        }
                    }
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle("History")
            .task { await viewModel.load() }
            .refreshable { await viewModel.load() }
            .overlay { if viewModel.isLoading { ProgressView().tint(SA.primary) } }
        }
    }

    private func row(_ entry: HistoryEntry) -> some View {
        HStack {
            Image(systemName: "smoke.fill")
                .foregroundStyle(SA.primary)
            Text(Self.timeFormatter.string(from: entry.date))
                .font(.saBodyLarge)
                .foregroundStyle(SA.onSurface)
            Spacer()
            if entry.minutesSincePrevious >= 0 {
                Text(gapText(entry.minutesSincePrevious))
                    .font(.saBodyMedium)
                    .foregroundStyle(SA.onSurfaceVariant)
            }
        }
    }

    private func gapText(_ minutes: Int64) -> String {
        let h = minutes / 60
        let m = minutes % 60
        return h > 0 ? "\(h)h \(m)m since last" : "\(m)m since last"
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "clock.badge.checkmark")
                .font(.system(size: 44))
                .foregroundStyle(SA.primary)
            Text("No cigarettes logged this month.")
                .font(.saBodyLarge)
                .foregroundStyle(SA.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }
}
