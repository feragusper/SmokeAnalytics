import SwiftUI

struct TriggerChipItem: Identifiable {
    let id: String       // trigger key
    let display: String  // "☕ Coffee"
}

/// Prompt shown right after logging a smoke: pick the triggers, or record no particular trigger.
struct RelationshipSheet: View {
    let options: [TriggerChipItem]
    let onSave: ([String]) -> Void
    let onSkip: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var selected: Set<String> = []

    private let columns = [GridItem(.adaptive(minimum: 104), spacing: 10)]

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(options) { option in
                            chip(option)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("What triggered it?")
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) {
                VStack(spacing: 8) {
                    Button {
                        onSave(Array(selected))
                        dismiss()
                    } label: {
                        Text(selected.isEmpty ? "Save" : "Save \(selected.count) tag\(selected.count == 1 ? "" : "s")")
                    }
                    .buttonStyle(SAPrimaryButtonStyle())
                    .disabled(selected.isEmpty)

                    Button("No particular trigger") {
                        onSkip()
                        dismiss()
                    }
                    .font(.saBodyLarge)
                    .foregroundStyle(SA.onSurfaceVariant)
                    .padding(.vertical, 6)
                }
                .padding(16)
                .background(SA.background)
            }
        }
    }

    private func chip(_ option: TriggerChipItem) -> some View {
        let isOn = selected.contains(option.id)
        return Button {
            if isOn { selected.remove(option.id) } else { selected.insert(option.id) }
        } label: {
            Text(option.display)
                .font(.saBodyMedium)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, 8)
                .foregroundStyle(isOn ? SA.onPrimary : SA.onSurface)
                .background(isOn ? SA.primary : SA.surfaceContainer, in: Capsule())
                .overlay(Capsule().strokeBorder(SA.outlineVariant, lineWidth: isOn ? 0 : 1))
        }
    }
}
