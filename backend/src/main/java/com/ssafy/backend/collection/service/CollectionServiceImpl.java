package com.ssafy.backend.collection.service;

import com.ssafy.backend.collection.dao.Achievement;
import com.ssafy.backend.collection.dao.Skin;
import com.ssafy.backend.collection.dao.Title;
import com.ssafy.backend.collection.dao.UserCollection;
import com.ssafy.backend.collection.dto.AchievementDto;
import com.ssafy.backend.collection.dto.SkinDto;
import com.ssafy.backend.collection.dto.TitleDto;
import com.ssafy.backend.collection.dto.UserCollectionDto;
import com.ssafy.backend.collection.repository.AchievementRepository;
import com.ssafy.backend.collection.repository.SkinRepository;
import com.ssafy.backend.collection.repository.TitleRepository;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.util.RandomNickname;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {

    private final SkinRepository skinRepository;
    private final TitleRepository titleRepository;
    private final AchievementRepository achievementRepository;
    private final UserCollectionRepository userCollectionRepository;
    private final UserProfileRepository userProfileRepository;

    public CollectionServiceImpl(SkinRepository skinRepository, TitleRepository titleRepository, AchievementRepository achievementRepository, UserCollectionRepository userCollectionRepository, UserProfileRepository userProfileRepository) {
        this.skinRepository = skinRepository;
        this.titleRepository = titleRepository;
        this.achievementRepository = achievementRepository;
        this.userCollectionRepository = userCollectionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // 유저에게 데이터 넣기
    public ResponseEntity<?> addSkin(UserAccessInfo userAccessInfo, int skinId){



        UserProfile userProfile=userAccessInfo.getUserProfile();
        String userCode=userProfile.getUserCode();
        UserCollection userCollection=userCollectionRepository.findById(userCode)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다."));
        Optional<Skin> newSkin=skinRepository.findById(skinId);
        if(newSkin.isPresent()){
            userCollection.getSkinIds().add(newSkin.get().getId());
            userCollectionRepository.save(userCollection);
            return ResponseEntity.ok().body("스킨 넣기 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "스킨이 없습니다."));
        }
    }
    public ResponseEntity<?> addTitle(){
        return ResponseEntity.ok("");
    }
    public ResponseEntity<?> addAchievement(){
        return ResponseEntity.ok("");
    }


    // 더미 데이터 추가
    public ResponseEntity<?> putSkin(){
        List<Skin> skins=new ArrayList<>();
        skins.add(new Skin(1,"빨강","1"));
        skins.add(new Skin(2,"주황","2"));
        skins.add(new Skin(3,"노랑","3"));
        skins.add(new Skin(4,"초록","4"));
        skins.add(new Skin(5,"파랑","5"));
        skinRepository.saveAll(skins);
        return ResponseEntity.ok("스킨 생성 완료");
    }
    public ResponseEntity<?> putTitle(){
        List<Title> titles=new ArrayList<>();
        titles.add(new Title(1,"첫번째","접속"));
        titles.add(new Title(2,"두번째","접속"));
        titles.add(new Title(3,"세번째","접속"));
        titles.add(new Title(4,"네번째","접속"));
        titles.add(new Title(5,"다섯번째","접속"));
        titleRepository.saveAll(titles);
        return ResponseEntity.ok("칭호 생성 완료");
    }
    public ResponseEntity<?> putAchievement(){
        List<Achievement> achievements=new ArrayList<>();
        achievements.add(new Achievement(1,"1","보상1"));
        achievements.add(new Achievement(2,"2","보상2"));
        achievements.add(new Achievement(3,"3","보상3"));
        achievements.add(new Achievement(4,"4","보상4"));
        achievements.add(new Achievement(5,"5","보상5"));
        achievementRepository.saveAll(achievements);
        return ResponseEntity.ok("업적 생성 완료");
    }



    public ResponseEntity<?> changeTitle(UserAccessInfo userAccessInfo, int titleId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        Optional<Title> changeTitle=titleRepository.findById(titleId);
        if(changeTitle.isPresent()){
            userProfile.setUserTitle(changeTitle.get().getName()); // 스킨 설정
            userProfileRepository.save(userProfile); // 변경 사항 저장
            return ResponseEntity.ok().body("칭호 변경 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "칭호가 없습니다."));
        }
    }

    public ResponseEntity<?> changeSkin(UserAccessInfo userAccessInfo, int skinId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        Optional<Skin> changeSkin=skinRepository.findById(skinId);
        if(changeSkin.isPresent()){
            userProfile.setUserSkin(changeSkin.get().getLink()); // 스킨 설정
            userProfileRepository.save(userProfile); // 변경 사항 저장
            return ResponseEntity.ok().body("스킨 변경 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "스킨이 없습니다."));
        }
    }

    public ResponseEntity<?> getCollections(UserAccessInfo userAccessInfo){
        String userCode=userAccessInfo.getUserProfile().getUserCode();
        UserCollection userCollection=userCollectionRepository.findById(userCode)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다."));

        // skindto
        List<Skin> allSkins=skinRepository.findAll();
        List<SkinDto> skinDtos=allSkins.stream().map(skin -> {
            SkinDto skinDto=new SkinDto();
            skinDto.setId(skin.getId());
            skinDto.setName(skin.getName());
            skinDto.setLink(skin.getLink());
            skinDto.setOwn(userCollection.getSkinIds().contains(skin.getId()));
            boolean isEquipped = skin.getLink().equals(userAccessInfo.getUserProfile().getUserSkin());
            skinDto.setEquipped(isEquipped);
            return skinDto;
        }).sorted((a,b)->Boolean.compare(b.isOwn(),a.isOwn())).toList();

        // titledto
        List<Title> allTitles=titleRepository.findAll();
        List<TitleDto> titleDtos = allTitles.stream().map(title -> {
            TitleDto titleDto = new TitleDto();
            titleDto.setId(title.getId());
            titleDto.setName(title.getName());
            titleDto.setCondition(title.getCondition());
            titleDto.setOwn(userCollection.getTitleIds().contains(title.getId()));
            // 현재 타이틀이 장착된 상태인지 설정
            boolean isEquipped = title.getName().equals(userAccessInfo.getUserProfile().getUserTitle());
            titleDto.setEquipped(isEquipped);
            return titleDto;
        }).sorted((a, b) -> Boolean.compare(b.isOwn(), a.isOwn())).toList();

        // achievementdto
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<AchievementDto> achievementDtos = allAchievements.stream().map(achievement -> {
            AchievementDto achievementDto = new AchievementDto();
            achievementDto.setId(achievement.getId());
            achievementDto.setName(achievement.getName());
            achievementDto.setReward(achievement.getReward());
            achievementDto.setAchieved(userCollection.getAchievementIds().contains(achievement.getId()));
            return achievementDto;
        }).sorted((a, b) -> Boolean.compare(b.isAchieved(), a.isAchieved())).toList();

        UserCollectionDto userCollectionDto=new UserCollectionDto();
        userCollectionDto.setSkins(skinDtos);
        userCollectionDto.setTitles(titleDtos);
        userCollectionDto.setAchievements(achievementDtos);

        return ResponseEntity.ok(userCollectionDto);
    }




}
