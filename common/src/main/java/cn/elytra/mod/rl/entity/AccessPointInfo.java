package cn.elytra.mod.rl.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
public class AccessPointInfo {

    public final String uuid;
    public final DimAndPos dimAndPos;

    @Nullable
    public String name;

}
