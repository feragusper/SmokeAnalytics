import SwiftUI

struct SignInView: View {
    @EnvironmentObject private var auth: AuthManager

    var body: some View {
        ZStack {
            SA.background.ignoresSafeArea()

            VStack(spacing: 20) {
                Spacer()

                Image("AppLogo")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 112, height: 112)
                    .clipShape(RoundedRectangle(cornerRadius: 25, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 25, style: .continuous)
                            .strokeBorder(SA.outlineVariant, lineWidth: 1)
                    )
                    .shadow(color: .black.opacity(0.08), radius: 12, y: 6)

                Text("Smoke Analytics")
                    .font(.system(size: 32, weight: .semibold))
                    .foregroundStyle(SA.onBackground)
                Text("Track your smoking, understand your habits.")
                    .font(.saBodyLarge)
                    .foregroundStyle(SA.onSurfaceVariant)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                Spacer()

                Button {
                    Task { await auth.signInWithGoogle() }
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "person.badge.key.fill")
                        Text("Sign in with Google")
                    }
                }
                .buttonStyle(SAPrimaryButtonStyle())
                .disabled(auth.isBusy)

                if let error = auth.errorText {
                    Text(error)
                        .font(.saBodyMedium)
                        .foregroundStyle(SA.error)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(32)

            if auth.isBusy {
                ProgressView().tint(SA.primary)
            }
        }
    }
}
