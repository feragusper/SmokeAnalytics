import SwiftUI

/// The four goal kinds, matching the shared `GoalTypeKeys` + `SmokingGoal` variants.
struct GoalKind: Identifiable {
    let key: String
    let title: String
    let unit: String
    let range: ClosedRange<Int>
    let step: Int
    let defaultValue: Int
    var id: String { key }

    static let all: [GoalKind] = [
        GoalKind(key: "daily_cap", title: "Daily cap", unit: "cigarettes / day", range: 1...40, step: 1, defaultValue: 10),
        GoalKind(key: "reduction_week", title: "Reduce vs last week", unit: "% fewer", range: 5...90, step: 5, defaultValue: 10),
        GoalKind(key: "reduction_month", title: "Reduce vs last month", unit: "% fewer", range: 5...90, step: 5, defaultValue: 10),
        GoalKind(key: "mindful_gap", title: "Mindful gap", unit: "min between", range: 15...240, step: 15, defaultValue: 60),
    ]

    static func of(_ key: String) -> GoalKind { all.first { $0.key == key } ?? all[0] }
}

struct GoalEditorSheet: View {
    let isEditing: Bool
    let onSave: (String, Int) -> Void
    let onClear: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var kind: GoalKind
    @State private var value: Int

    init(prefillTypeKey: String, prefillValue: Int, isEditing: Bool,
         onSave: @escaping (String, Int) -> Void, onClear: @escaping () -> Void) {
        self.isEditing = isEditing
        self.onSave = onSave
        self.onClear = onClear
        let k = prefillTypeKey.isEmpty ? GoalKind.all[0] : GoalKind.of(prefillTypeKey)
        _kind = State(initialValue: k)
        _value = State(initialValue: prefillValue > 0 ? prefillValue : k.defaultValue)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 16) {
                        typeCard
                        valueCard
                    }
                    .padding(16)
                }
            }
            .navigationTitle(isEditing ? "Edit goal" : "Set a goal")
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) {
                VStack(spacing: 8) {
                    Button {
                        onSave(kind.key, value)
                        dismiss()
                    } label: { Text("Save goal") }
                    .buttonStyle(SAPrimaryButtonStyle())

                    if isEditing {
                        Button("Clear goal", role: .destructive) {
                            onClear()
                            dismiss()
                        }
                        .font(.saBodyLarge)
                        .padding(.vertical, 6)
                    }
                }
                .padding(16)
                .background(SA.background)
            }
        }
    }

    private var typeCard: some View {
        SACard {
            VStack(alignment: .leading, spacing: 12) {
                Text("Goal type").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                ForEach(GoalKind.all) { option in
                    Button {
                        kind = option
                        if !option.range.contains(value) { value = option.defaultValue }
                    } label: {
                        HStack {
                            Text(option.title)
                                .font(.saBodyLarge)
                                .foregroundStyle(SA.onSurface)
                            Spacer()
                            Image(systemName: kind.key == option.key ? "largecircle.fill.circle" : "circle")
                                .foregroundStyle(kind.key == option.key ? SA.primary : SA.outline)
                        }
                        .padding(.vertical, 6)
                    }
                }
            }
        }
    }

    private var valueCard: some View {
        SACard {
            VStack(alignment: .leading, spacing: 12) {
                Text("Target").font(.saLabelMedium).foregroundStyle(SA.onSurfaceVariant)
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text("\(value)")
                        .font(.system(size: 40, weight: .bold))
                        .foregroundStyle(SA.primary)
                    Text(kind.unit)
                        .font(.saBodyLarge)
                        .foregroundStyle(SA.onSurfaceVariant)
                }
                Stepper("", value: $value, in: kind.range, step: kind.step)
                    .labelsHidden()
            }
        }
    }
}
