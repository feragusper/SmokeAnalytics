import SwiftUI
import Shared

struct DayEntry: Identifiable {
    let id: String
    let date: Date
    let minutesSincePrevious: Int64
}

@MainActor
final class ArchiveViewModel: ObservableObject {
    @Published var year: Int
    @Published var month: Int
    @Published var selectedDay: Int
    @Published var dayCounts: [Int: Int] = [:]
    @Published var maxCount = 0
    @Published var dayEntries: [DayEntry] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let facade = HistoryFacade()

    init() {
        let now = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        year = now.year ?? 2026
        month = now.month ?? 1
        selectedDay = now.day ?? 1
    }

    func loadMonth() async {
        isLoading = true
        errorText = nil
        do {
            let counts = try await facade.monthCounts(year: Int32(year), month: Int32(month))
            var dict: [Int: Int] = [:]
            for c in counts { dict[Int(c.day)] = Int(c.count) }
            dayCounts = dict
            maxCount = dict.values.max() ?? 0
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
        await loadDay()
    }

    func loadDay() async {
        do {
            let items = try await facade.smokesForDay(year: Int32(year), month: Int32(month), day: Int32(selectedDay))
            dayEntries = items.map {
                DayEntry(id: $0.id,
                         date: Date(timeIntervalSince1970: Double($0.epochMillis) / 1000.0),
                         minutesSincePrevious: $0.minutesSincePrevious)
            }
        } catch {
            errorText = String(describing: error)
        }
    }

    func select(day: Int) {
        selectedDay = day
        Task { await loadDay() }
    }

    func shiftMonth(_ delta: Int) {
        var m = month + delta
        var y = year
        if m < 1 { m = 12; y -= 1 }
        if m > 12 { m = 1; y += 1 }
        month = m; year = y
        selectedDay = 1
        Task { await loadMonth() }
    }

    func delete(_ id: String) async {
        do {
            try await facade.delete(id: id)
            await loadMonth()
        } catch { errorText = String(describing: error) }
    }

    func editSmoke(_ id: String, to date: Date) async {
        do {
            try await facade.editSmoke(id: id, epochMillis: Int64(date.timeIntervalSince1970 * 1000))
            await loadMonth()
        } catch { errorText = String(describing: error) }
    }

    func addSmoke(at date: Date) async {
        do {
            try await facade.addSmokeAt(epochMillis: Int64(date.timeIntervalSince1970 * 1000))
            await loadMonth()
        } catch { errorText = String(describing: error) }
    }

    /// The selected day at the current time — the default when adding a cigarette manually.
    var selectedDayNow: Date {
        var comps = DateComponents(year: year, month: month, day: selectedDay)
        let t = Calendar.current.dateComponents([.hour, .minute], from: Date())
        comps.hour = t.hour; comps.minute = t.minute
        return Calendar.current.date(from: comps) ?? Date()
    }

    // MARK: calendar geometry (Monday-first, like Android)

    var daysInMonth: Int {
        let comps = DateComponents(year: year, month: month)
        guard let date = Calendar.current.date(from: comps),
              let range = Calendar.current.range(of: .day, in: .month, for: date) else { return 30 }
        return range.count
    }

    var leadingEmptySlots: Int {
        let comps = DateComponents(year: year, month: month, day: 1)
        guard let first = Calendar.current.date(from: comps) else { return 0 }
        let weekday = Calendar.current.component(.weekday, from: first) // 1=Sun..7=Sat
        return (weekday + 5) % 7 // Monday=0
    }

    var monthTitle: String {
        let comps = DateComponents(year: year, month: month)
        guard let date = Calendar.current.date(from: comps) else { return "" }
        let f = DateFormatter(); f.dateFormat = "MMMM yyyy"
        return f.string(from: date).capitalized
    }

    var selectedDateTitle: String {
        let comps = DateComponents(year: year, month: month, day: selectedDay)
        guard let date = Calendar.current.date(from: comps) else { return "" }
        let f = DateFormatter(); f.dateFormat = "EEEE, d MMM yyyy"
        return f.string(from: date).capitalized
    }
}

/// "The Archive": a month calendar with per-day counts, and the selected day's log.
struct HistoryView: View {
    @StateObject private var viewModel = ArchiveViewModel()
    @State private var showAdd = false
    @State private var editingEntry: DayEntry?

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 4), count: 7)
    private let weekdays = ["M", "T", "W", "T", "F", "S", "S"]

    private static let timeFormatter: DateFormatter = {
        let f = DateFormatter(); f.timeStyle = .short; return f
    }()

    var body: some View {
        NavigationStack {
            List {
                Section {
                    calendarCard
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                }

                Section {
                    if viewModel.dayEntries.isEmpty {
                        Text("No cigarettes logged this day.")
                            .font(.saBodyLarge).foregroundStyle(SA.onSurfaceVariant)
                    } else {
                        ForEach(viewModel.dayEntries) { entry in
                            Button { editingEntry = entry } label: { entryRow(entry) }
                                .tint(SA.onSurface)
                                .swipeActions {
                                    Button(role: .destructive) {
                                        Task { await viewModel.delete(entry.id) }
                                    } label: { Label("Delete", systemImage: "trash") }
                                }
                        }
                    }
                } header: {
                    dayHeader
                }
            }
            .listStyle(.insetGrouped)
            .scrollContentBackground(.hidden)
            .background(SA.background)
            .navigationTitle("The Archive")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showAdd = true } label: { Image(systemName: "plus") }
                        .tint(SA.primary)
                }
            }
            .task { await viewModel.loadMonth() }
            .overlay { if viewModel.isLoading { ProgressView().tint(SA.primary) } }
            .sheet(isPresented: $showAdd) {
                TimePickerSheet(title: "Add a cigarette", initial: viewModel.selectedDayNow) { date in
                    Task { await viewModel.addSmoke(at: date) }
                }
            }
            .sheet(item: $editingEntry) { entry in
                TimePickerSheet(title: "Edit time", initial: entry.date) { date in
                    Task { await viewModel.editSmoke(entry.id, to: date) }
                }
            }
        }
    }

    private func entryRow(_ entry: DayEntry) -> some View {
        HStack {
            Image(systemName: "smoke.fill").foregroundStyle(SA.primary)
            Text(Self.timeFormatter.string(from: entry.date))
                .font(.saBodyLarge).foregroundStyle(SA.onSurface)
            Spacer()
            if entry.minutesSincePrevious >= 0 {
                Text(gapText(entry.minutesSincePrevious))
                    .font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
            }
        }
    }

    private var calendarCard: some View {
        SACard(cornerRadius: 24) {
            VStack(spacing: 12) {
                HStack {
                    Button { viewModel.shiftMonth(-1) } label: { Image(systemName: "chevron.left") }
                    Spacer()
                    Text(viewModel.monthTitle).font(.saTitleMedium).foregroundStyle(SA.onSurface)
                    Spacer()
                    Button { viewModel.shiftMonth(1) } label: { Image(systemName: "chevron.right") }
                }
                .tint(SA.primary)

                LazyVGrid(columns: columns, spacing: 4) {
                    ForEach(0..<7, id: \.self) { i in
                        Text(weekdays[i]).font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                    }
                }
                LazyVGrid(columns: columns, spacing: 4) {
                    ForEach(Array(daySlots.enumerated()), id: \.offset) { _, day in
                        if let day { dayCell(day) } else { Color.clear.frame(height: 40) }
                    }
                }
            }
        }
    }

    /// Leading blanks (Monday-first) followed by the month's days.
    private var daySlots: [Int?] {
        Array(repeating: nil, count: viewModel.leadingEmptySlots) + (1...viewModel.daysInMonth).map { Optional($0) }
    }

    private func dayCell(_ day: Int) -> some View {
        let count = viewModel.dayCounts[day] ?? 0
        let isSelected = day == viewModel.selectedDay
        let intensity = viewModel.maxCount > 0 ? Double(count) / Double(viewModel.maxCount) : 0
        return Button { viewModel.select(day: day) } label: {
            VStack(spacing: 1) {
                Text("\(day)")
                    .font(.saBodyMedium)
                    .foregroundStyle(isSelected ? SA.onPrimary : SA.onSurface)
                Text(count > 0 ? "\(count)" : " ")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(isSelected ? SA.onPrimary.opacity(0.9) : SA.primary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 42)
            .background(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .fill(isSelected ? SA.primary : SA.primary.opacity(count > 0 ? 0.10 + 0.18 * intensity : 0))
            )
        }
    }

    private var dayHeader: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(viewModel.selectedDateTitle).font(.saTitleMedium).foregroundStyle(SA.onSurface)
            Text("\(viewModel.dayEntries.count) \(viewModel.dayEntries.count == 1 ? "cigarette" : "cigarettes")")
                .font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
        }
        .padding(.top, 4)
    }

    private func gapText(_ minutes: Int64) -> String {
        let h = minutes / 60, m = minutes % 60
        return h > 0 ? "\(h)h \(m)m" : "\(m)m"
    }
}

/// Date+time picker sheet used to add a cigarette at a time or edit an existing one's time.
struct TimePickerSheet: View {
    let title: String
    let onSave: (Date) -> Void
    @State private var date: Date
    @Environment(\.dismiss) private var dismiss

    init(title: String, initial: Date, onSave: @escaping (Date) -> Void) {
        self.title = title
        self.onSave = onSave
        _date = State(initialValue: initial)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                DatePicker("When", selection: $date, in: ...Date(), displayedComponents: [.date, .hourAndMinute])
                    .datePickerStyle(.graphical)
                    .tint(SA.primary)
                    .padding()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(date); dismiss() }
                }
            }
            .tint(SA.primary)
        }
    }
}
