package org.valkyrienskies.mod.compat.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay.ShipShaderOverlayData;

public class VsShipShaderOverlays {
    private static final List<VsShipShaderOverlay> overlayList = new ArrayList<>();

    public static void registerOverlay(final VsShipShaderOverlay overlay) {
        overlayList.add(overlay);
    }

    public static Optional<ShipShaderOverlayData> processAllOverlays(final ClientShip ship) {
        Optional<ShipShaderOverlayData> overlayData = Optional.empty();
        for (final VsShipShaderOverlay overlay : overlayList) {
            overlayData = overlay.processShaderForShip(ship, overlayData.orElse(null));
        }
        return overlayData;
    }
}
