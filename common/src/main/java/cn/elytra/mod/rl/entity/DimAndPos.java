package cn.elytra.mod.rl.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO for DimensionalCoords
 */
@Data
@RequiredArgsConstructor
public class DimAndPos {
    public final int dim;
    public final int x, y, z;
}
