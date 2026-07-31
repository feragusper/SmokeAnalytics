import Foundation
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import UIKit

/// Owns the iOS auth flow: Google Sign-In → FirebaseAuth Google credential. The shared Kotlin
/// repositories read the same `Firebase.auth.currentUser`, so once this signs in, Home works.
@MainActor
final class AuthManager: ObservableObject {
    @Published var isSignedIn: Bool = false
    @Published var displayName: String?
    @Published var errorText: String?
    @Published var isBusy: Bool = false

    private var listener: AuthStateDidChangeListenerHandle?

    init() {
        AppBootstrap.ensure()
        let user = Auth.auth().currentUser
        isSignedIn = user != nil
        displayName = user?.displayName
        listener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.isSignedIn = user != nil
            self?.displayName = user?.displayName
        }
    }

    func signInWithGoogle() async {
        isBusy = true
        errorText = nil
        defer { isBusy = false }
        do {
            guard let clientID = FirebaseApp.app()?.options.clientID else {
                throw AuthError.message("Missing Firebase clientID")
            }
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)

            guard let presenter = Self.topViewController() else {
                throw AuthError.message("No presenting view controller")
            }
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: presenter)
            guard let idToken = result.user.idToken?.tokenString else {
                throw AuthError.message("Missing Google id token")
            }
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: result.user.accessToken.tokenString
            )
            try await Auth.auth().signIn(with: credential)
        } catch {
            errorText = error.localizedDescription
        }
    }

    func signOut() {
        GIDSignIn.sharedInstance.signOut()
        try? Auth.auth().signOut()
    }

    private enum AuthError: LocalizedError {
        case message(String)
        var errorDescription: String? {
            switch self { case .message(let m): return m }
        }
    }

    /// The frontmost view controller to present the Google sheet from.
    static func topViewController() -> UIViewController? {
        let windowScene = UIApplication.shared.connectedScenes
            .first { $0.activationState == .foregroundActive } as? UIWindowScene
        var top = windowScene?.keyWindow?.rootViewController
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }
}
