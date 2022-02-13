package com.lindum.mlkit.vision.dev.posedetector;

import java.util.List;

public class PoseLibrary {
    List<PoseEntity> poseEntityList;

    public PoseLibrary(List<PoseEntity> poseEntities) {
        poseEntityList = poseEntities;
    }

    public void init(List<PoseEntity> poseEntities) {
        poseEntityList = poseEntities;
    }
}
