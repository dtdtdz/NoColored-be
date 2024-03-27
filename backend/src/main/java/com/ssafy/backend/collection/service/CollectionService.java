package com.ssafy.backend.collection.service;

import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;

public interface CollectionService {

    ResponseEntity<?> getCollections(UserAccessInfo userAccessInfo);
    ResponseEntity<?> changeSkin(UserAccessInfo userAccessInfo, int skinId);
    ResponseEntity<?> changeTitle(UserAccessInfo userAccessInfo, int titleId);
    ResponseEntity<?> putSkin();
    ResponseEntity<?> putTitle();
    ResponseEntity<?> putAchievement();
    ResponseEntity<?> addSkin(UserAccessInfo userAccessInfo, int skinId);
    ResponseEntity<?> addTitle();
    ResponseEntity<?> addAchievement();

}
