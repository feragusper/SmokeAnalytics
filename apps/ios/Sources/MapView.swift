import SwiftUI
import MapKit
import Shared

struct SmokePin: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
    let date: Date
}

@MainActor
final class MapViewModel: ObservableObject {
    @Published var pins: [SmokePin] = []
    @Published var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 40.4168, longitude: -3.7038),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    @Published var isLoading = false

    private let facade = MapFacade()

    func load() async {
        isLoading = true
        let locs = (try? await facade.locatedSmokes()) ?? []
        pins = locs.map {
            SmokePin(
                coordinate: CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude),
                date: Date(timeIntervalSince1970: Double($0.epochMillis) / 1000.0)
            )
        }
        if let first = pins.first {
            region = MKCoordinateRegion(
                center: first.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
            )
        }
        isLoading = false
    }
}

/// Where cigarettes were logged, when location tracking is on. Reached from Analytics.
struct MapView: View {
    @StateObject private var viewModel = MapViewModel()

    var body: some View {
        ZStack {
            if viewModel.pins.isEmpty && !viewModel.isLoading {
                SA.background.ignoresSafeArea()
                VStack(spacing: 12) {
                    Image(systemName: "mappin.slash").font(.system(size: 44)).foregroundStyle(SA.primary)
                    Text("No location data yet")
                        .font(.saTitleMedium).foregroundStyle(SA.onSurface)
                    Text("Turn on location tracking in Settings to see where you smoke.")
                        .font(.saBodyLarge).foregroundStyle(SA.onSurfaceVariant)
                        .multilineTextAlignment(.center).padding(.horizontal, 40)
                }
            } else {
                Map(coordinateRegion: $viewModel.region, annotationItems: viewModel.pins) { pin in
                    MapMarker(coordinate: pin.coordinate, tint: SA.primary)
                }
                .ignoresSafeArea(edges: .bottom)
            }
        }
        .navigationTitle("Map")
        .navigationBarTitleDisplayMode(.inline)
        .task { await viewModel.load() }
    }
}
