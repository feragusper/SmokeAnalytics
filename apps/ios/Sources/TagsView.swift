import SwiftUI
import Shared

struct TagItem: Identifiable {
    let key: String
    let display: String
    let isCustom: Bool
    let hidden: Bool
    var id: String { key }
}

@MainActor
final class TagsViewModel: ObservableObject {
    @Published var tags: [TagItem] = []
    @Published var newTag = ""
    @Published var errorText: String?

    private let facade = PreferencesFacade()

    var builtins: [TagItem] { tags.filter { !$0.isCustom } }
    var customs: [TagItem] { tags.filter { $0.isCustom } }

    func load() async {
        do {
            tags = try await facade.tags().map {
                TagItem(key: $0.key, display: $0.display, isCustom: $0.isCustom, hidden: $0.hidden)
            }
        } catch { errorText = String(describing: error) }
    }

    func addCustom() async {
        let label = newTag.trimmingCharacters(in: .whitespaces)
        guard !label.isEmpty else { return }
        newTag = ""
        do { try await facade.addCustomTag(label: label); await load() }
        catch { errorText = String(describing: error) }
    }

    func removeCustom(_ key: String) async {
        do { try await facade.removeCustomTag(key: key); await load() }
        catch { errorText = String(describing: error) }
    }

    func setHidden(_ key: String, _ hidden: Bool) async {
        do { try await facade.setBuiltinHidden(key: key, hidden: hidden); await load() }
        catch { errorText = String(describing: error) }
    }
}

/// Manage the trigger tags shown in the "I smoked" prompt: add/remove custom tags, hide built-ins.
struct TagsView: View {
    @StateObject private var viewModel = TagsViewModel()

    var body: some View {
        List {
            Section("Add a tag") {
                HStack {
                    TextField("New tag", text: $viewModel.newTag)
                    Button("Add") { Task { await viewModel.addCustom() } }
                        .disabled(viewModel.newTag.trimmingCharacters(in: .whitespaces).isEmpty)
                        .tint(SA.primary)
                }
            }

            if !viewModel.customs.isEmpty {
                Section("Your tags") {
                    ForEach(viewModel.customs) { tag in
                        Text(tag.display)
                            .swipeActions {
                                Button(role: .destructive) {
                                    Task { await viewModel.removeCustom(tag.key) }
                                } label: { Label("Delete", systemImage: "trash") }
                            }
                    }
                }
            }

            Section("Built-in tags") {
                ForEach(viewModel.builtins) { tag in
                    Toggle(isOn: Binding(
                        get: { !tag.hidden },
                        set: { shown in Task { await viewModel.setHidden(tag.key, !shown) } }
                    )) {
                        Text(tag.display)
                    }
                    .tint(SA.primary)
                }
            }

            if let error = viewModel.errorText {
                Section { Text(error).font(.saBodyMedium).foregroundStyle(SA.error) }
            }
        }
        .navigationTitle("Tags")
        .scrollContentBackground(.hidden)
        .background(SA.background)
        .task { await viewModel.load() }
    }
}
