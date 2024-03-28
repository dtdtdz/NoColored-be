package com.ssafy.backend.collection.service;

import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;

public interface CollectionService {

    ResponseEntity<?> getCollections(UserAccessInfo userAccessInfo);
    ResponseEntity<?> changeSkin(UserAccessInfo userAccessInfo, int skinId);
    ResponseEntity<?> changeLabel(UserAccessInfo userAccessInfo, int labelId);
    ResponseEntity<?> putSkin();
    ResponseEntity<?> putLabel();
    ResponseEntity<?> putAchievement();
    ResponseEntity<?> addSkin(UserAccessInfo userAccessInfo, int skinId);
    ResponseEntity<?> addLabel(UserAccessInfo userAccessInfo, int labelId);
    ResponseEntity<?> addAchievement(UserAccessInfo userAccessInfo, int achievementId);

    ResponseEntity<?> deleteSkin();
    ResponseEntity<?> deleteLabel();
    ResponseEntity<?> deleteAchievement();

}
