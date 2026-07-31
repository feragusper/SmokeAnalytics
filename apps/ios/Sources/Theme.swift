import SwiftUI

/// SwiftUI mirror of the shared design system (Android `PaletteTokens` / M3 scheme, web tokens).
/// Colors adapt to light/dark automatically via a dynamic `UIColor`, matching the Android
/// `lightColorScheme` / `darkColorScheme` value-for-value so the three platforms read as one app.
enum SA {

    // MARK: Colors (light / dark)

    static let primary            = dyn(0x2E7A84, 0x97D7DE)
    static let onPrimary          = dyn(0xFFFFFF, 0x19444A)
    static let primaryContainer   = dyn(0xD0EFF3, 0x255F67)
    static let onPrimaryContainer = dyn(0x0F2D31, 0xD0EFF3)

    static let background         = dyn(0xF6F8F8, 0x071112)
    static let onBackground       = dyn(0x142022, 0xE6EFEF)

    static let surface            = dyn(0xFFFFFF, 0x0D1416)
    static let onSurface          = dyn(0x142022, 0xC1D1D4)
    /// M3 `surfaceContainerLow` — the card fill used across Home.
    static let surfaceContainer   = dyn(0xF1F4F4, 0x121A1C)
    static let surfaceVariant     = dyn(0xEBF1F2, 0x152124)
    static let onSurfaceVariant   = dyn(0x5B6F73, 0x9EB1B4)

    static let secondaryContainer = dyn(0xC1D1D4, 0x415154)

    static let outline            = dyn(0xB4C2C5, 0x3D4D50)
    static let outlineVariant     = dyn(0xD7E1E3, 0x263437)

    static let error              = dyn(0xBA1A1A, 0xFFB4AB)

    private static func dyn(_ light: Int, _ dark: Int) -> Color {
        Color(uiColor: UIColor { traits in
            UIColor(rgb: traits.userInterfaceStyle == .dark ? dark : light)
        })
    }
}

private extension UIColor {
    convenience init(rgb: Int) {
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255,
            green: CGFloat((rgb >> 8) & 0xFF) / 255,
            blue: CGFloat(rgb & 0xFF) / 255,
            alpha: 1
        )
    }
}

// MARK: - M3 typography scale (subset used by the iOS screens)

extension Font {
    static let saDisplayLarge  = Font.system(size: 57, weight: .regular)
    static let saHeadlineSmall = Font.system(size: 24, weight: .regular)
    static let saTitleLarge    = Font.system(size: 22, weight: .regular)
    static let saTitleMedium   = Font.system(size: 16, weight: .medium)
    static let saLabelLarge    = Font.system(size: 14, weight: .medium)
    static let saLabelMedium   = Font.system(size: 12, weight: .medium)
    static let saBodyLarge     = Font.system(size: 16, weight: .regular)
    static let saBodyMedium    = Font.system(size: 14, weight: .regular)
}

// MARK: - Card container (M3 rounded surface + hairline outline, like Home's section cards)

struct SACard<Content: View>: View {
    var cornerRadius: CGFloat = 24
    @ViewBuilder var content: Content

    var body: some View {
        content
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(SA.surfaceContainer, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(SA.outlineVariant, lineWidth: 1)
            )
    }
}

// MARK: - Primary button styling (filled, pill, primary/onPrimary)

struct SAPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        SAPrimaryButtonBody(configuration: configuration)
    }

    private struct SAPrimaryButtonBody: View {
        let configuration: Configuration
        // isEnabled is only readable from a View, not from makeBody directly.
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            configuration.label
                .font(.saTitleMedium)
                .foregroundStyle(SA.onPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(SA.primary, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
                .opacity(!isEnabled ? 0.4 : (configuration.isPressed ? 0.85 : 1))
        }
    }
}
