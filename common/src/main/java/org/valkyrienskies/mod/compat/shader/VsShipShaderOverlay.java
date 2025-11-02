package org.valkyrienskies.mod.compat.shader;

import java.util.Optional;
import javax.annotation.Nullable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay.ShipShaderOverlayData;

public interface VsShipShaderOverlay {
    Optional<ShipShaderOverlayData> processShaderForShip(ClientShip ship, @Nullable ShipShaderOverlayData shader);
}
