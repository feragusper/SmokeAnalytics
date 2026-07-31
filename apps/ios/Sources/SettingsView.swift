import SwiftUI
import Shared

@MainActor
final class SettingsViewModel: ObservableObject {
    @Published var nickname = ""
    @Published var quitReason = ""
    @Published var packPrice = 0.0
    @Published var cigarettesPerPack = 20
    @Published var currencySymbol = "€"
    @Published var dayStartHour = 6
    @Published var bedtimeHour = 22
    @Published var weekStartsMonday = true
    @Published var use24HourClock = true
    @Published var locationTrackingEnabled = false

    @Published var isLoading = false
    @Published var isSaving = false
    @Published var errorText: String?
    @Published var savedTick = false

    private let facade = PreferencesFacade()

    func load() async {
        isLoading = true
        errorText = nil
        do {
            let p = try await facade.load()
            nickname = p.nickname
            quitReason = p.quitReason
            packPrice = p.packPrice
            cigarettesPerPack = Int(p.cigarettesPerPack)
            currencySymbol = p.currencySymbol
            dayStartHour = Int(p.dayStartHour)
            bedtimeHour = Int(p.bedtimeHour)
            weekStartsMonday = p.weekStartsMonday
            use24HourClock = p.use24HourClock
            locationTrackingEnabled = p.locationTrackingEnabled
        } catch {
            errorText = String(describing: error)
        }
        isLoading = false
    }

    func save() async {
        isSaving = true
        errorText = nil
        do {
            try await facade.save(dto: PreferencesDTO(
                nickname: nickname,
                quitReason: quitReason,
                packPrice: packPrice,
                cigarettesPerPack: Int32(cigarettesPerPack),
                currencySymbol: currencySymbol,
                dayStartHour: Int32(dayStartHour),
                bedtimeHour: Int32(bedtimeHour),
                weekStartsMonday: weekStartsMonday,
                use24HourClock: use24HourClock,
                locationTrackingEnabled: locationTrackingEnabled
            ))
            savedTick.toggle()
        } catch {
            errorText = String(describing: error)
        }
        isSaving = false
    }
}

/// The "You" tab: account summary, editable preferences, sign out.
struct SettingsView: View {
    @EnvironmentObject private var auth: AuthManager
    @StateObject private var viewModel = SettingsViewModel()
    @AppStorage("themeMode") private var themeMode = "system"

    private let hours = Array(0..<24)
    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack(spacing: 12) {
                        Image(systemName: "person.crop.circle.fill")
                            .font(.system(size: 34))
                            .foregroundStyle(SA.primary)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(auth.displayName ?? "Signed in").font(.saTitleMedium)
                            Text("Google account").font(.saBodyMedium).foregroundStyle(SA.onSurfaceVariant)
                        }
                    }
                }

                Section("Profile") {
                    TextField("Nickname", text: $viewModel.nickname)
                    TextField("Reason to quit", text: $viewModel.quitReason, axis: .vertical)
                }

                Section("Cost") {
                    HStack {
                        Text("Pack price")
                        Spacer()
                        TextField("0", value: $viewModel.packPrice, format: .number)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 90)
                    }
                    Stepper("Cigarettes per pack: \(viewModel.cigarettesPerPack)",
                            value: $viewModel.cigarettesPerPack, in: 1...60)
                    HStack {
                        Text("Currency symbol")
                        Spacer()
                        TextField("€", text: $viewModel.currencySymbol)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 60)
                    }
                }

                Section("Day") {
                    Picker("Day starts at", selection: $viewModel.dayStartHour) {
                        ForEach(hours, id: \.self) { Text(hourLabel($0)).tag($0) }
                    }
                    Picker("Bedtime", selection: $viewModel.bedtimeHour) {
                        ForEach(hours, id: \.self) { Text(hourLabel($0)).tag($0) }
                    }
                    Toggle("Week starts on Monday", isOn: $viewModel.weekStartsMonday)
                    Toggle("24-hour clock", isOn: $viewModel.use24HourClock)
                }

                Section("Appearance") {
                    Picker("Theme", selection: $themeMode) {
                        Text("System").tag("system")
                        Text("Light").tag("light")
                        Text("Dark").tag("dark")
                    }
                }

                Section("Privacy") {
                    Toggle("Track location of cigarettes", isOn: $viewModel.locationTrackingEnabled)
                }

                Section("About") {
                    ShareLink(item: URL(string: "https://feragusper.github.io/SmokeAnalytics/")!) {
                        Label("Share the app", systemImage: "square.and.arrow.up")
                    }
                    Link(destination: URL(string: "https://feragusper.github.io/SmokeAnalytics/")!) {
                        Label("About", systemImage: "info.circle")
                    }
                    HStack {
                        Text("Version")
                        Spacer()
                        Text(appVersion).foregroundStyle(SA.onSurfaceVariant)
                    }
                }

                if let error = viewModel.errorText {
                    Section { Text(error).font(.saBodyMedium).foregroundStyle(SA.error) }
                }

                Section {
                    Button("Sign out", role: .destructive) { auth.signOut() }
                }
            }
            .scrollContentBackground(.hidden)
            .background(SA.background)
            .navigationTitle("You")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    if viewModel.isSaving {
                        ProgressView()
                    } else {
                        Button("Save") { Task { await viewModel.save() } }
                            .tint(SA.primary)
                    }
                }
            }
            .task { await viewModel.load() }
        }
        .tint(SA.primary)
    }

    private func hourLabel(_ h: Int) -> String {
        if viewModel.use24HourClock { return String(format: "%02d:00", h) }
        let period = h < 12 ? "AM" : "PM"
        let twelve = h % 12 == 0 ? 12 : h % 12
        return "\(twelve) \(period)"
    }
}
