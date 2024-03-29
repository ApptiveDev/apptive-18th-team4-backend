package com.hot6.pnureminder.controller;

import com.hot6.pnureminder.dto.LectureRoom.*;
import com.hot6.pnureminder.entity.Building;
import com.hot6.pnureminder.entity.Member;
import com.hot6.pnureminder.repository.BuildingRepository;
import com.hot6.pnureminder.service.BuildingService;
import com.hot6.pnureminder.service.Favorite.FavoriteBuildingService;
import com.hot6.pnureminder.service.LectureRoomService;
import com.hot6.pnureminder.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lecture-rooms")
public class LectureRoomController {

    private final LectureRoomService lectureRoomService;
    private final MemberService memberService;
    private final BuildingService buildingService;
    private final BuildingRepository buildingRepository;
    private final FavoriteBuildingService favoriteBuildingService;

    @GetMapping("/available")
    public ResponseEntity<BuildingResponseDto> getAvailableLectureRoomsAndLectures(
            @RequestParam String buildingName,
            @RequestParam Integer setTime) {

        Building building = buildingRepository.findByBuildingName(buildingName)
                .orElseThrow(() -> new RuntimeException("찾는 건물이 없습니다."));


        LectureRoomRequestDto requestDto = LectureRoomRequestDto.builder()
                .buildingName(buildingName)
                .currentTime(LocalTime.of(14, 40))  //임시 시간
                .setTime(setTime)
//                .dayOfWeek(DateTimeUtilsForTest.getCurrentDayOfWeekAsInt())
                .dayOfWeek(0)
                .build();

        List<AvailableNowDto> nowAvailableResponse= lectureRoomService.getNowAvailableLectureRoomsAndLectures(requestDto);
//        List<AvailableSoonDto> soonAvailableResponse = lectureRoomService.getSoonAvailableLectureRoomsAndLectures(requestDto);

        BuildingResponseDto buildingResponseDto = BuildingResponseDto.builder()
                .buildingName(buildingName)
                .availableNow(nowAvailableResponse)
//                .availableSoon(soonAvailableResponse)
                .buildingLat(building.getBuildingLat())
                .buildingLng(building.getBuildingLng())
                .build();

        return ResponseEntity.ok(buildingResponseDto);
    }

    @GetMapping("/available-list")
    public ResponseEntity<List<BuildingResponseDto>> getListOfAvailableLectureRoomsAndLectures(
            @RequestParam("user_latitude") double latitude,
            @RequestParam("user_longitude") double longitude,
            @RequestParam Integer setTime
    ) {
        List<Building> buildings = buildingService.findNearestBuildings(latitude, longitude);
        List<BuildingResponseDto> responses = new ArrayList<>();

        for (Building building : buildings) {
            String buildingName = building.getBuildingName();

            LectureRoomRequestDto requestDto = LectureRoomRequestDto.builder()
                    .buildingName(buildingName)
                    .currentTime(LocalTime.of(14, 40))  //임시 시간
                    .setTime(setTime)
                    .dayOfWeek(0)
                    .build();

            List<AvailableNowDto> nowAvailableResponse= lectureRoomService.getNowAvailableLectureRoomsAndLectures(requestDto);

            if (!nowAvailableResponse.isEmpty()) {
                BuildingResponseDto buildingResponseDto = BuildingResponseDto.builder()
                        .buildingName(buildingName)
                        .availableNow(nowAvailableResponse)
                        .buildingLat(building.getBuildingLat())
                        .buildingLng(building.getBuildingLng())
                        .build();
                responses.add(buildingResponseDto);
            }
        }

        return ResponseEntity.ok(responses);
    }
    @GetMapping("/favorite-list")
    public ResponseEntity<List<BuildingResponseDto>> getListOfFavoriteLectureRoomsAndLectures(
            @RequestParam("user_latitude") double latitude,
            @RequestParam("user_longitude") double longitude,
            @RequestParam Integer setTime
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Member member = memberService.findMemberByUsername(username);
        List<Building> buildings = favoriteBuildingService.findBuildingByMember(member);
        List<Building> allNearestBuildings = buildingService.findNearestBuildings(latitude, longitude);

        // 사용자의 즐겨찾기 건물만 필터링
        List<Building> favoriteNearestBuildings = allNearestBuildings.stream()
                .filter(building -> buildings.contains(building))
                .collect(Collectors.toList());
        List<BuildingResponseDto> responses = new ArrayList<>();

        for (Building building : favoriteNearestBuildings) {
            String buildingName = building.getBuildingName();

            LectureRoomRequestDto requestDto = LectureRoomRequestDto.builder()
                    .buildingName(buildingName)
                    .currentTime(LocalTime.of(14, 40))  //임시 시간
                    .setTime(setTime)
                    .dayOfWeek(0)
                    .build();

            List<AvailableNowDto> nowAvailableResponse= lectureRoomService.getNowAvailableLectureRoomsAndLectures(requestDto);

            if (!nowAvailableResponse.isEmpty()) {
                BuildingResponseDto buildingResponseDto = BuildingResponseDto.builder()
                        .buildingName(buildingName)
                        .availableNow(nowAvailableResponse)
                        .buildingLat(building.getBuildingLat())
                        .buildingLng(building.getBuildingLng())
                        .build();
                responses.add(buildingResponseDto);
            }
        }

        return ResponseEntity.ok(responses);
    }

}

/**@GetMapping("/available-dsl")
public ResponseEntity<BuildingResponseDto> getAvailableRooms(
        @RequestParam String buildingName,
        @RequestParam Integer setTime) {

    LectureRoomRequestDto requestDto = LectureRoomRequestDto.builder()
            .buildingName(buildingName)
            .currentTime(LocalTime.of(14, 40))  //임시 시간
            .setTime(setTime)
            .dayOfWeek(DateTimeUtilsForTest.getCurrentDayOfWeekAsInt())
            .dayOfWeek(0)
            .build();

    BuildingResponseDto buildingResponseDto = lectureRoomService.getAvailableRooms(requestDto);
    return ResponseEntity.ok(buildingResponseDto);
}**/
