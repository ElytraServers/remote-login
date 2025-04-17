package cn.elytra.mod.rl.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AccessPointInfo {

    public final String uuid;
    public final DimAndPos dimAndPos;

}
