package com.hot6.pnureminder.repository;

import com.hot6.pnureminder.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Integer> {

    List<Lecture> findAllByLectureRoomId (Integer lectureRoomId);

    List<Lecture> findAllByLectureRoomIdAndDayOfWeek(Integer lectureRoomId, Integer dayOfWeek);

    Optional<Lecture> findLectureById(Integer id);

}
