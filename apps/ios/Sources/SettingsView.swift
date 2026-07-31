import SwiftUI

/// The "You" tab: account summary + sign out. Preferences (theme, language, goals config) come later.
struct SettingsView: View {
    @EnvironmentObject private var auth: AuthManager

    var body: some View {
        NavigationStack {
            ZStack {
                SA.background.ignoresSafeArea()

                VStack(spacing: 16) {
                    SACard {
                        HStack(spacing: 14) {
                            Image(systemName: "person.crop.circle.fill")
                                .font(.system(size: 40))
                                .foregroundStyle(SA.primary)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(auth.displayName ?? "Signed in")
                                    .font(.saTitleMedium)
                                    .foregroundStyle(SA.onSurface)
                                Text("Google account")
                                    .font(.saBodyMedium)
                                    .foregroundStyle(SA.onSurfaceVariant)
                            }
                            Spacer()
                        }
                    }

                    Button {
                        auth.signOut()
                    } label: {
                        HStack(spacing: 10) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Sign out")
                        }
                    }
                    .buttonStyle(SAPrimaryButtonStyle())

                    Spacer()
                }
                .padding(16)
            }
            .navigationTitle("You")
        }
    }
}
