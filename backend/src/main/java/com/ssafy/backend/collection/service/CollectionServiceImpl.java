package com.ssafy.backend.collection.service;

import com.ssafy.backend.collection.document.Achievement;
import com.ssafy.backend.collection.document.Skin;
import com.ssafy.backend.collection.document.Label;
import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.collection.dto.AchievementDto;
import com.ssafy.backend.collection.dto.SkinDto;
import com.ssafy.backend.collection.dto.LabelDto;
import com.ssafy.backend.collection.dto.UserCollectionDto;
import com.ssafy.backend.collection.repository.AchievementRepository;
import com.ssafy.backend.collection.repository.SkinRepository;
import com.ssafy.backend.collection.repository.LabelRepository;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.user.repository.UserProfileRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CollectionServiceImpl implements CollectionService {

    private final SkinRepository skinRepository;
    private final LabelRepository labelRepository;
    private final AchievementRepository achievementRepository;
    private final UserCollectionRepository userCollectionRepository;
    private final UserProfileRepository userProfileRepository;

    public CollectionServiceImpl(SkinRepository skinRepository, LabelRepository labelRepository, AchievementRepository achievementRepository, UserCollectionRepository userCollectionRepository, UserProfileRepository userProfileRepository) {
        this.skinRepository = skinRepository;
        this.labelRepository = labelRepository;
        this.achievementRepository = achievementRepository;
        this.userCollectionRepository = userCollectionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // mongodb에서 데이터 삭제
    public ResponseEntity<?> deleteSkin(){
        skinRepository.deleteAll();
        return ResponseEntity.ok("스킨 삭제 완료");
    }
    public ResponseEntity<?> deleteLabel(){
        labelRepository.deleteAll();
        return ResponseEntity.ok("칭호 삭제 완료");
    }
    public ResponseEntity<?> deleteAchievement(){
        achievementRepository.deleteAll();
        return ResponseEntity.ok("업적 삭제 완료");
    }
    
    
    // 유저에게 데이터 넣기
    public ResponseEntity<?> addSkin(UserAccessInfo userAccessInfo, int skinId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        String userCode=userProfile.getUserCode();
        UserCollection userCollection=userCollectionRepository.findById(userCode)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다."));
        Optional<Skin> newSkin=skinRepository.findById(skinId);
        if(newSkin.isPresent()){
            // 가지고 있는 스킨이면 안넣기
            if(userCollection.getSkinIds().contains(skinId)){
                return ResponseEntity.badRequest().body("이미 가지고 있는 스킨입니다");
            }
            userCollection.getSkinIds().add(newSkin.get().getId());
            userCollectionRepository.save(userCollection);
            return ResponseEntity.ok().body("스킨 넣기 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "스킨이 없습니다."));
        }
    }
    public ResponseEntity<?> addLabel(UserAccessInfo userAccessInfo, int labelId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        String userCode=userProfile.getUserCode();
        UserCollection userCollection=userCollectionRepository.findById(userCode)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다."));
        Optional<Label> newLabel=labelRepository.findById(labelId);
        if(newLabel.isPresent()){
            // 가지고 있는 칭호면 안넣기
            if(userCollection.getLabelIds().contains(labelId)){
                return ResponseEntity.badRequest().body("이미 가지고 있는 칭호입니다");
            }
            userCollection.getLabelIds().add(newLabel.get().getId());
            userCollectionRepository.save(userCollection);
            return ResponseEntity.ok().body("칭호 넣기 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "칭호가 없습니다."));
        }
    }
    public ResponseEntity<?> addAchievement(UserAccessInfo userAccessInfo, int achievementId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        String userCode=userProfile.getUserCode();
        UserCollection userCollection=userCollectionRepository.findById(userCode)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다."));
        Optional<Achievement> newAchievement=achievementRepository.findById(achievementId);
        if(newAchievement.isPresent()){
            // 가지고 있는 업적이면 안넣기
            if(userCollection.getAchievementIds().contains(achievementId)){
                return ResponseEntity.badRequest().body("이미 가지고 있는 업적입니다");
            }
            userCollection.getAchievementIds().add(newAchievement.get().getId());
            userCollectionRepository.save(userCollection);
            return ResponseEntity.ok().body("업적 넣기 완료!");
        }else{
            return ResponseEntity.badRequest().body(Map.of("error", "업적이 없습니다."));
        }
    }


    // 더미 데이터 추가
    public ResponseEntity<?> putSkin(){
        List<Skin> skins=new ArrayList<>();
        skins.add(new Skin(1,"character-240px-sheet-pastelyellow.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-pastelyellow.png"));
        skins.add(new Skin(2,"character-240px-sheet-pastelred.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-pastelred.png"));
        skins.add(new Skin(3,"character-240px-sheet-pastelpink.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-pastelpink.png"));
        skins.add(new Skin(4,"character-240px-sheet-pastelgreen.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-pastelgreen.png"));
        skins.add(new Skin(5,"character-240px-sheet-pastelblue.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-pastelblue.png"));
        skins.add(new Skin(6,"character-240px-sheet-npcWhite.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-npcWhite.png"));
        skins.add(new Skin(7,"character-240px-sheet-googlered.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-googlered.png"));
        skins.add(new Skin(8,"character-240px-sheet-googleorange.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-googleorange.png"));
        skins.add(new Skin(9,"character-240px-sheet-googlegreen.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-googlegreen.png"));
        skins.add(new Skin(10,"character-240px-sheet-googleblue.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-googleblue.png"));
        skins.add(new Skin(11,"character-240px-sheet-basicyellow.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicyellow.png"));
        skins.add(new Skin(12,"character-240px-sheet-basicyellow-sunglass.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicyellow-sunglass.png"));
        skins.add(new Skin(13,"character-240px-sheet-basicyellow-magichat.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicyellow-magichat.png"));
        skins.add(new Skin(14,"character-240px-sheet-basicyellow-butterfly.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicyellow-butterfly.png"));
        skins.add(new Skin(15,"character-240px-sheet-basicred.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicred.png"));
        skins.add(new Skin(16,"character-240px-sheet-basicred-sunglass.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicred-sunglass.png"));
        skins.add(new Skin(17,"character-240px-sheet-basicred-magichat.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicred-magichat.png"));
        skins.add(new Skin(18,"character-240px-sheet-basicred-butterfly.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicred-butterfly.png"));
        skins.add(new Skin(19,"character-240px-sheet-basicpink.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicpink.png"));
        skins.add(new Skin(20,"character-240px-sheet-basicpink-sunglass.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicpink-sunglass.png"));
        skins.add(new Skin(21,"character-240px-sheet-basicpink-magichat.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicpink-magichat.png"));
        skins.add(new Skin(22,"character-240px-sheet-basicpink-butterfly.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicpink-butterfly.png"));
        skins.add(new Skin(23,"character-240px-sheet-basicgreen.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicgreen.png"));
        skins.add(new Skin(24,"character-240px-sheet-basicgreen-sunglass.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicgreen-sunglass.png"));
        skins.add(new Skin(25,"character-240px-sheet-basicgreen-magichat.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicgreen-magichat.png"));
        skins.add(new Skin(26,"character-240px-sheet-basicgreen-butterfly.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicgreen-butterfly.png"));
        skins.add(new Skin(27,"character-240px-sheet-basicblue.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue.png"));
        skins.add(new Skin(28,"character-240px-sheet-basicblue-sunglass.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue-sunglass.png"));
        skins.add(new Skin(29,"character-240px-sheet-basicblue-magichat.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue-magichat.png"));
        skins.add(new Skin(30,"character-240px-sheet-basicblue-butterfly.png","https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue-butterfly.png"));
        skinRepository.saveAll(skins);
        return ResponseEntity.ok("스킨 생성 완료");
    }
    public ResponseEntity<?> putLabel(){
        List<Label> labels =new ArrayList<>();
        labels.add(new Label(1,"시작이 반"));
        labels.add(new Label(2,"승리의 V"));
        labels.add(new Label(3,"작심삼일"));
        labels.add(new Label(4,"사랑"));
        labels.add(new Label(5,"오메가"));
        labels.add(new Label(6,"주말엔 쉬었죠?"));
        labels.add(new Label(7,"두자리수"));
        labels.add(new Label(8,"보름달"));
        labels.add(new Label(9,"한달이나?"));
        labels.add(new Label(10,"1등을 향해서"));
        labels.add(new Label(11,"2렇게 초조한데"));
        labels.add(new Label(12,"3 Color"));
        labels.add(new Label(13,"4랑해 널 사랑해"));
        labels.add(new Label(14,"5늘은 말할거야"));
        labels.add(new Label(15,"6십억 지구는 옛말"));
        labels.add(new Label(16,"7 럭키야"));
        labels.add(new Label(17,"8딱팔딱 뛰는 가슴"));
        labels.add(new Label(18,"9해줘 오 내 마음"));
        labels.add(new Label(19,"10년이 가도 너를 사랑해"));
        labels.add(new Label(20,"노래가 끝나도 이어지는 게임"));
        labels.add(new Label(21,"벌써 2주나?"));
        labels.add(new Label(22,"문어 다리 두마리"));
        labels.add(new Label(23,"꾸준함의 왕"));
        labels.add(new Label(24,"노컬러랜드라고 외쳐도 안가져요"));
        labels.add(new Label(25,"단판승부"));
        labels.add(new Label(26,"승부는 삼세판"));
        labels.add(new Label(27,"벌써 5판"));
        labels.add(new Label(28,"늘어가는 판수"));
        labels.add(new Label(29,"한 달만  더, 한 판만 더"));
        labels.add(new Label(30,"갓겜등극?"));
        labels.add(new Label(31,"노컬러랜드 명예 주민"));
        labels.add(new Label(32,"이것이 승리입니다."));
        labels.add(new Label(33,"너? 재능있어"));
        labels.add(new Label(34,"3연승을 향해"));
        labels.add(new Label(35,"오는데 힘들었죠?"));
        labels.add(new Label(36,"럭키 세븐"));
        labels.add(new Label(37,"노컬러드 프로게이머"));
        labels.add(new Label(38,"칼드컵 우승을 향해"));
        labels.add(new Label(39,"패배자"));
        labels.add(new Label(40,"야 바로 매칭 돌려."));
        labels.add(new Label(41,"작심삼일"));
        labels.add(new Label(42,"너 나보다 못하잖아."));
        labels.add(new Label(43,"7전8기"));
        labels.add(new Label(44,"아낌없이 주는 나무"));
        labels.add(new Label(45,"점프 뉴비"));
        labels.add(new Label(46,"학교 폭력은 아닙니다"));
        labels.add(new Label(47,"자라나는 새싹 밟기"));
        labels.add(new Label(48,"점프 마스터"));
        labels.add(new Label(49,"뛰는 놈 위에 나는 놈"));
        labels.add(new Label(50,"하나도 안아픈데?"));
        labels.add(new Label(51,"다시 일어나면 됩니다"));
        labels.add(new Label(52,"쟤 나보다 못하는데,,,"));
        labels.add(new Label(53,"쭈글쭈글"));
        labels.add(new Label(54,"키컸으면"));
        labels.add(new Label(55,"살아남기의 신"));
        labels.add(new Label(56,"알 수 없는 힘"));
        labels.add(new Label(57,"너만을 원해"));
        labels.add(new Label(58,"마이 프레셔스!!"));
        labels.add(new Label(59,"너만 보면 도파민"));
        labels.add(new Label(60,"와우"));
        labels.add(new Label(61,"밥 먹으러 갑시다!"));
        labels.add(new Label(62,"타임머신"));
        labels.add(new Label(63,"정말 고마워요"));
        labels.add(new Label(64,"전설의 시작"));
        labels.add(new Label(65,"욕심쟁이"));
        labels.add(new Label(66,"공산주의 노컬러랜드"));
        labels.add(new Label(67,"게임하긴 했나요?"));
        labels.add(new Label(68,"너 없으면 게임 망해"));
        labels.add(new Label(69,"파리 목숨"));
        labels.add(new Label(70,"바로 저희에요!"));
        labels.add(new Label(71,"손님"));
        labels.add(new Label(72,"파릇파릇 새싹"));
        labelRepository.saveAll(labels);
        return ResponseEntity.ok("칭호 생성 완료");
    }
    public ResponseEntity<?> putAchievement(){
        List<Achievement> achievements=new ArrayList<>();
        achievements.add(new Achievement(1, "누적 접속 1일", "Basic Blue Magichat"));
        achievements.add(new Achievement(2, "누적 접속 2일", "Basic Blue Butterfly"));
        achievements.add(new Achievement(3, "누적 접속 3일", "Basic Green Magichat"));
        achievements.add(new Achievement(4, "누적 접속 4일", "Basic Green Butterfly"));
        achievements.add(new Achievement(5, "누적 접속 5일", "오메가"));
        achievements.add(new Achievement(6, "누적 접속 7일", "주말엔 쉬었죠?"));
        achievements.add(new Achievement(7, "누적 접속 10일", "두자리수"));
        achievements.add(new Achievement(8, "누적 접속 15일", "보름달"));
        achievements.add(new Achievement(9, "누적 접속 30일", "한달이나?"));
        achievements.add(new Achievement(10, "연속 접속 1일", "Basic Pink Magichat"));
        achievements.add(new Achievement(11, "연속 접속 2일", "Basic Pink Butterfly"));
        achievements.add(new Achievement(12, "연속 접속 3일", "Basic Yellow Magichat"));
        achievements.add(new Achievement(13, "연속 접속 4일", "Basic Yellow Butterfly"));
        achievements.add(new Achievement(14, "연속 접속 5일", "5늘은 말할거야"));
        achievements.add(new Achievement(15, "연속 접속 6일", "6십억 지구는 옛말"));
        achievements.add(new Achievement(16, "연속 접속 7일", "7 럭키야"));
        achievements.add(new Achievement(17, "연속 접속 8일", "8딱팔딱 뛰는 가슴"));
        achievements.add(new Achievement(18, "연속 접속 9일", "9해줘 오 내 마음"));
        achievements.add(new Achievement(19, "연속 접속 10일", "10년이 가도 너를 사랑해"));
        achievements.add(new Achievement(20, "연속 접속 12일", "노래가 끝나도 이어지는 게임"));
        achievements.add(new Achievement(21, "연속 접속 14일", "벌써 2주나?"));
        achievements.add(new Achievement(22, "연속 접속 16일", "문어 다리 두마리"));
        achievements.add(new Achievement(23, "연속 접속 18일", "꾸준함의 왕"));
        achievements.add(new Achievement(24, "연속 접속 20일", "노컬러랜드라고 외쳐도 안가져요"));
        achievements.add(new Achievement(25, "누적 플레이 수 1판", "Pastel Yellow"));
        achievements.add(new Achievement(26, "누적 플레이 수 3판", "Pastel Red"));
        achievements.add(new Achievement(27, "누적 플레이 수 5판", "Pastel Pink"));
        achievements.add(new Achievement(28, "누적 플레이 수 10판", "Pastel Green"));
        achievements.add(new Achievement(29, "누적 플레이 수 30판", "한 달만  더, 한 판만 더"));
        achievements.add(new Achievement(30, "누적 플레이 수 50판", "갓겜등극?"));
        achievements.add(new Achievement(31, "누적 플레이 수 100판", "노컬러랜드 명예 주민"));
        achievements.add(new Achievement(32, "누적 1승", "Pastel Blue"));
        achievements.add(new Achievement(33, "누적 2승", "Google Red"));
        achievements.add(new Achievement(34, "누적 3승", "Google Orange"));
        achievements.add(new Achievement(35, "누적 5승", "Google Green"));
        achievements.add(new Achievement(36, "누적 7승", "Google Blue"));
        achievements.add(new Achievement(37, "누적 10승", "Basic Blue Sunglass"));
        achievements.add(new Achievement(38, "누적 20승", "칼드컵 우승을 향해"));
        achievements.add(new Achievement(39, "누적 1패", "패배자"));
        achievements.add(new Achievement(40, "누적 2패", "Basic Green Sunglass"));
        achievements.add(new Achievement(41, "누적 3패", "작심삼일"));
        achievements.add(new Achievement(42, "누적 5패", "Basic Red Sunglass"));
        achievements.add(new Achievement(43, "누적 7패", "7전8기"));
        achievements.add(new Achievement(44, "누적 10패", "아낌없이 주는 나무"));
        achievements.add(new Achievement(45, "누적 5번 밟기", "점프 뉴비"));
        achievements.add(new Achievement(46, "누적 10번 밟기", "학교 폭력은 아닙니다"));
        achievements.add(new Achievement(47, "누적 20번 밟기", "자라나는 새싹 밟기"));
        achievements.add(new Achievement(48, "누적 50번 밟기", "점프 마스터"));
        achievements.add(new Achievement(49, "누적 100번 밟기", "뛰는 놈 위에 나는 놈"));
        achievements.add(new Achievement(50, "누적 5번 밟힘", "하나도 안아픈데?"));
        achievements.add(new Achievement(51, "누적 10번 밟힘", "다시 일어나면 됩니다"));
        achievements.add(new Achievement(52, "누적 50번 밟힘", "쟤 나보다 못하는데,,,"));
        achievements.add(new Achievement(53, "누적 100번 밟힘", "쭈글쭈글"));
        achievements.add(new Achievement(54, "누적 200번 밟힘", "키컸으면"));
        achievements.add(new Achievement(55, "단판 0데스", "살아남기의 신"));
        achievements.add(new Achievement(56, "아이템 처음 1회 획득", "Basic Red Butterfly"));
        achievements.add(new Achievement(57, "아이템 누적 5회 획득", "Basic Pink Sunglass"));
        achievements.add(new Achievement(58, "아이템 누적 10회 획득", "Basic Red Magichat"));
        achievements.add(new Achievement(59, "아이템 누적 20회 획득", "너만 보면 도파민"));
        achievements.add(new Achievement(60, "누적 1시간", "와우"));
        achievements.add(new Achievement(61, "누적 2시간", "밥 먹으러 갑시다!"));
        achievements.add(new Achievement(62, "누적 5시간", "타임머신"));
        achievements.add(new Achievement(63, "누적 10시간", "정말 고마워요"));
        achievements.add(new Achievement(64, "게스트 로그인 회원 전환", "Basic Yellow Sunglass"));
        achievements.add(new Achievement(65, "단판, 혼자만 아이템 독식", " 욕심쟁이"));
        achievements.add(new Achievement(66, "모두 점수가 같은 상태로 마무리", "공산주의 노컬러랜드"));
        achievements.add(new Achievement(67, "모두 점수가 0점인 상태로 마무리", "게임하긴 했나요?"));
        achievements.add(new Achievement(68, "연속 로그인 끊김", "너 없으면 게임 망해"));
        achievements.add(new Achievement(69, "게임 시작 5초만에 밟힘", "파리 목숨"));
        achievements.add(new Achievement(70, "만든 사람들 버튼 클릭", "바로 저희에요!"));
        achievements.add(new Achievement(71, "게스트 회원가입", "손님"));
        achievements.add(new Achievement(72, "유저 회원가입", "파릇파릇 새싹"));
        achievementRepository.saveAll(achievements);
        return ResponseEntity.ok("업적 생성 완료");
    }



    public ResponseEntity<?> changeLabel(UserAccessInfo userAccessInfo, int labelId){
        UserProfile userProfile=userAccessInfo.getUserProfile();
        Optional<Label> changeLabel=labelRepository.findById(labelId);
        if(changeLabel.isPresent()){
            userProfile.setUserLabel(changeLabel.get().getName()); // 스킨 설정
            userProfileRepository.save(userProfile); // 변경 사항 저장
            UserProfileDto userProfileDto=userAccessInfo.getUserProfileDto();
            userProfileDto.setLabel(changeLabel.get().getName());
            userAccessInfo.setUserProfileDto(userProfileDto);
            userAccessInfo.setUserProfile(userProfile);
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
            UserProfileDto userProfileDto=userAccessInfo.getUserProfileDto();
            userProfileDto.setSkin(changeSkin.get().getLink());
            userAccessInfo.setUserProfileDto(userProfileDto);
            userAccessInfo.setUserProfile(userProfile);
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

        // labeldto
        List<Label> allLabels =labelRepository.findAll();
        List<LabelDto> labelDtos = allLabels.stream().map(label -> {
            LabelDto labelDto = new LabelDto();
            labelDto.setId(label.getId());
            labelDto.setName(label.getName());
            labelDto.setOwn(userCollection.getLabelIds().contains(label.getId()));
            // 현재 타이틀이 장착된 상태인지 설정
            boolean isEquipped = label.getName().equals(userAccessInfo.getUserProfile().getUserLabel());
            labelDto.setEquipped(isEquipped);
            return labelDto;
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
        userCollectionDto.setLabels(labelDtos);
        userCollectionDto.setAchievements(achievementDtos);

        return ResponseEntity.ok(userCollectionDto);
    }
}
